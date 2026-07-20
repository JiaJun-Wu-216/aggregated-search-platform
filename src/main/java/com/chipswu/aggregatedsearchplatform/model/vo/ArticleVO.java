package com.chipswu.aggregatedsearchplatform.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.chipswu.aggregatedsearchplatform.model.entity.Article;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章信息视图类
 *
 * @author WuJiaJun
 */
@Data
public class ArticleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2962491960090345621L;

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 高亮标题
     */
    private String highlightTitle;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    public static ArticleVO objToVo(Article article) {
        ArticleVO articleVO = new ArticleVO();
        BeanUtil.copyProperties(article, articleVO);
        return articleVO;
    }
}
