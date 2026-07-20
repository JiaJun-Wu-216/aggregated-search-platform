package com.chipswu.aggregatedsearchplatform.datasource;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONUtil;
import com.chipswu.aggregatedsearchplatform.exception.BusinessException;
import com.chipswu.aggregatedsearchplatform.exception.ErrorCode;
import com.chipswu.aggregatedsearchplatform.model.entity.Picture;
import com.chipswu.aggregatedsearchplatform.model.enums.SearchTypeEnum;
import com.mybatisflex.core.paginate.Page;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 图片服务实现类
 *
 * @author WuJiaJun
 */
@Slf4j
@Service
public class PictureDataSource implements DataSource<Picture> {
    /**
     * 搜索
     *
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<Picture> doSearch(String searchText, int pageNum, int pageSize) {
        int current = (pageNum - 1) * pageSize;
        String url = String.format("https://cn.bing.com/images/search?q=%s&first=%s", searchText, current);
        RetryConfig retryConfig = RetryConfig.custom()
                // 重试 3 次
                .maxAttempts(3)
                // 每次重试间隔 1 秒
                .waitDuration(Duration.ofSeconds(1L))
                // 当出现 IOException、TimeoutException 时进行重试
                .retryExceptions(IOException.class, TimeoutException.class)
                // 当出现 BusinessException 不再重试
                .ignoreExceptions(BusinessException.class)
                // 每次重试失败后，等待时间按公式 min(initial * multiplier^(attempt-1), max) 递增
                .intervalFunction(IntervalFunction.ofExponentialBackoff(1000, 2.0, 10000))
                .build();
        // 2. 创建 Retry 实例（name 用于监控指标区分）
        Retry retry = Retry.of("bingImageSearch", retryConfig);
        Document doc = Try.ofSupplier(Retry.decorateSupplier(retry, () -> {
                    try {
                        return Jsoup.connect(url)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                                .timeout(5000)
                                .get();
                    } catch (IOException e) {
                        throw new RuntimeException(e); // decorateSupplier 需要 unchecked exception
                    }
                }))
                .getOrElseThrow(throwable -> {
                    // 所有重试耗尽后仍失败，转为业务异常
                    return new BusinessException(ErrorCode.SYSTEM_ERROR, "数据获取异常，已重试3次");
                });
        Elements elements = doc.select(".iuscp.isv");
        List<Picture> pictures = new ArrayList<>();
        for (Element element : elements) {
            // 取图片地址（murl）
            String m = element.select(".iusc").getFirst().attr("m");
            Map<String, Object> map;
            try {
                map = JSONUtil.toBean(
                        m,
                        new TypeReference<>() {
                        },
                        false
                );
            } catch (JSONException e) {
                log.error("Bing图片元数据JSON解析失败: {}", m, e);
                return null;
            }
            String murl = (String) map.get("murl");
            // 取标题
            String title = element.select(".inflnk").getFirst().attr("aria-label");
            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
            if (pictures.size() >= pageSize) {
                break;
            }
        }
        Page<Picture> picturePage = new Page<>(pageNum, pageSize);
        picturePage.setRecords(pictures);
        return picturePage;
    }

    @Override
    public String getType() {
        return SearchTypeEnum.PICTURE.getValue();
    }
}
