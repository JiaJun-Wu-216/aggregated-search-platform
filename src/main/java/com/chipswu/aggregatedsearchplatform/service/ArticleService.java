package com.chipswu.aggregatedsearchplatform.service;

import com.chipswu.aggregatedsearchplatform.model.dto.ArticleQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.entity.Article;
import com.chipswu.aggregatedsearchplatform.model.vo.ArticleVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 文章相关应用层接口
 *
 * @author WuJiaJun
 */
public interface ArticleService extends IService<Article> {

    Page<Article> searchFromEs(ArticleQueryRequest articleQueryRequest);

    Page<ArticleVO> getArticleVOPage(Page<Article> articlePage);
}
