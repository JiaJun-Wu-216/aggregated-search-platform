package com.chipswu.aggregatedsearchplatform;

import cn.hutool.core.util.RandomUtil;
import com.chipswu.aggregatedsearchplatform.mapper.ArticleMapper;
import com.chipswu.aggregatedsearchplatform.model.entity.Article;
import com.mybatisflex.core.update.UpdateChain;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.chipswu.aggregatedsearchplatform.model.entity.table.ArticleTableDef.ARTICLE;

/**
 *
 * @author WuJiaJun
 */
@Slf4j
@SpringBootTest
public class GetArticleTest {

    @Resource
    private ArticleMapper articleMapper;

    @Data
    static class HotRankItem {
        private int rank;
        private String title;
        private String href;
        private String author;
        private int likes;
        private int comments;
        private int heat;

        public HotRankItem(int rank, String title, String href, String author,
                       int likes, int comments, int heat) {
            this.rank = rank;
            this.title = title;
            this.href = href;
            this.author = author;
            this.likes = likes;
            this.comments = comments;
            this.heat = heat;
        }
    }

    @Test
    public void scrapeFromRscPayload() throws IOException {
        List<HotRankItem> items = new ArrayList<>();
        // 方式B：本地文件调试（取消注释即可）
         Document doc = Jsoup.parse(
             new File("C:\\Users\\test\\Desktop\\passage.html"), "UTF-8");
        Elements links = doc.select("a[href^=\"/post/\"], a[href^=\"/essay/\"]");

        for (Element link : links) {
            String href = link.attr("href");
            Element titleEl = link.selectFirst("div.line-clamp-1");
            if (titleEl == null) continue;

            String title = titleEl.text();
            Element rankEl = link.selectFirst("strong.text-lg.w-5");
            int rank = rankEl != null ? parseIntSafe(rankEl.text()) : 0;
            Element heatEl = link.selectFirst("strong.text-lg.text-gray-900");
            int heat = heatEl != null ? parseIntSafe(heatEl.text()) : 0;

            int likes = 0, comments = 0;
            for (Element sp : link.select("span.text-xs")) {
                String t = sp.text();
                if (t.contains("点赞"))
                    likes = parseIntSafe(t.replace("点赞", "").trim());
                else if (t.contains("评论"))
                    comments = parseIntSafe(t.replace("评论", "").trim());
            }

            items.add(new HotRankItem(rank, title, "https://code-nav.cn" + href,
                    "", likes, comments, heat));
        }
        for (int i = 0; i < items.size(); i++) {
            log.info("结果 - {}：{}",i, items.get(i));
        }
        List<Article> articleList = items.stream().map(item -> Article.builder()
                .title(item.getTitle())
                .author(RandomUtil.randomString(5))
                .build()).toList();
        articleMapper.insertBatch(articleList);
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    @Test
    public void updateArticle(){
        boolean result = UpdateChain.of(Article.class)
                .set(ARTICLE.TITLE, RandomUtil.randomString(5))
                .where(ARTICLE.ID.eq(436143298034274304L))
                .update();
    }
}
