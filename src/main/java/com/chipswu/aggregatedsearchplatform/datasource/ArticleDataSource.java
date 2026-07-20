package com.chipswu.aggregatedsearchplatform.datasource;

import com.chipswu.aggregatedsearchplatform.model.dto.ArticleQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.entity.Article;
import com.chipswu.aggregatedsearchplatform.model.enums.SearchTypeEnum;
import com.chipswu.aggregatedsearchplatform.model.vo.ArticleVO;
import com.chipswu.aggregatedsearchplatform.service.ArticleService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 *
 * @author WuJiaJun
 */
@Slf4j
@Service
public class ArticleDataSource implements DataSource<ArticleVO> {

    @Resource
    private ArticleService articleService;

    /**
     * 搜索
     *
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<ArticleVO> doSearch(String searchText, int pageNum, int pageSize) {
        ArticleQueryRequest articleQueryRequest = new ArticleQueryRequest();
        articleQueryRequest.setSearchText(searchText);
        articleQueryRequest.setPageNum(pageNum);
        articleQueryRequest.setPageSize(pageSize);
        Page<Article> articlePage = articleService.searchFromEs(articleQueryRequest);
        return articleService.getArticleVOPage(articlePage);
    }

    @Override
    public String getType() {
        return SearchTypeEnum.ARTICLE.getValue();
    }
}
