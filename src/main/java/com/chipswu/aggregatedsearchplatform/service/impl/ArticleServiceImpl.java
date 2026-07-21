package com.chipswu.aggregatedsearchplatform.service.impl;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.FieldSuggesterBuilders;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.core.search.TermSuggester;
import com.chipswu.aggregatedsearchplatform.constant.CommonConstant;
import com.chipswu.aggregatedsearchplatform.mapper.ArticleMapper;
import com.chipswu.aggregatedsearchplatform.model.dto.ArticleEsDTO;
import com.chipswu.aggregatedsearchplatform.model.dto.ArticleQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.entity.Article;
import com.chipswu.aggregatedsearchplatform.model.vo.ArticleVO;
import com.chipswu.aggregatedsearchplatform.service.ArticleService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章相关应用层接口实现类
 *
 * @author WuJiaJun
 */
@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public Page<Article> searchFromEs(ArticleQueryRequest articleQueryRequest) {
        Long id = articleQueryRequest.getId();
        String searchText = articleQueryRequest.getSearchText();
        String title = articleQueryRequest.getTitle();
        // es 起始页为 0
        int currentPage = articleQueryRequest.getPageNum() - 1;
        int pageSize = articleQueryRequest.getPageSize();
        String sortField = articleQueryRequest.getSortField();
        String sortOrder = articleQueryRequest.getSortOrder();

        // 1. 动态收集 filter 条件
        List<Query> filters = new ArrayList<>();

        if (id != null) {
            filters.add(Query.of(q -> q.term(t -> t.field("id").value(id))));
        }

        // 2. 动态收集 should 条件（修复原代码 minimumShouldMatch 覆盖问题）
        List<Query> shouldClauses = new ArrayList<>();

        if (StringUtils.isNotBlank(searchText)) {
            shouldClauses.add(Query.of(q -> q.match(m -> m.field("title").query(searchText))));
        }
        if (StringUtils.isNotBlank(title)) {
            shouldClauses.add(Query.of(q -> q.match(m -> m.field("title").query(title))));
        }

        // 3. 组装 Bool Query
        BoolQuery boolQuery = BoolQuery.of(b -> {
            b.filter(filters);
            if (!shouldClauses.isEmpty()) {
                b.should(shouldClauses);
                b.minimumShouldMatch("1");
            } else {
                // 关键修复：无任何搜索条件时，显式匹配所有文档，避免纯 filter + score 排序导致的异常行为
                b.must(m -> m.matchAll(ma -> ma));
            }
            return b;
        });

        // 3.1 配置高亮
        HighlightFieldParameters titleParameters = HighlightFieldParameters.builder()
                .withPreTags("<em>")
                .withPostTags("</em>")
                .withFragmentSize(100)
                .withNumberOfFragments(3)
                .build();
        HighlightField titleField = new HighlightField("title", titleParameters);
        Highlight highlight = new Highlight(List.of(titleField));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, ArticleEsDTO.class);

        // 3.2 配置搜索建议（仅在有 searchText 时生效）
        TermSuggester termSuggester = FieldSuggesterBuilders.term()
                .field("title")          // 指定字段
                .build();

        // 构建 FieldSuggester（包含 text 和 term）
        FieldSuggester fieldSuggester = new FieldSuggester.Builder()
                .text(searchText)            // 对应 suggest.title-suggest.text
                .term(termSuggester)
                .build();

        // 构建 Suggester（命名建议器为 "title-suggest"）
        Suggester suggester = Suggester.of(s -> s
                .suggesters("title-suggest", fieldSuggester)
        );

        // 4. 排序（替代 SortBuilder）
        List<SortOptions> sortOptions = new ArrayList<>();
        if (StringUtils.isNotBlank(sortField)) {
            SortOrder order = CommonConstant.SORT_ORDER_ASC.equals(sortOrder)
                    ? SortOrder.Asc : SortOrder.Desc;
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field(sortField).order(order))));
        } else if (!shouldClauses.isEmpty()) {
            // 只有存在文本搜索时才按评分排序
            sortOptions.add(SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc))));
        } else {
            // 纯过滤查询时，必须有确定性排序字段，否则分页结果不稳定
            sortOptions.add(SortOptions.of(s -> s.field(f -> f.field("id").order(SortOrder.Desc))));
        }

        // 5. 构建 NativeQuery（替代 NativeSearchQueryBuilder）
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(boolQuery._toQuery())
                .withHighlightQuery(highlightQuery)
                .withSuggester(suggester)
                .withSort(sortOptions)
                .withPageable(PageRequest.of(currentPage, pageSize))
                .build();

        // 6. 执行查询（ElasticsearchOperations 替代 ElasticsearchRestTemplate）
        SearchHits<ArticleEsDTO> searchHits = elasticsearchOperations.search(nativeQuery, ArticleEsDTO.class);

        // 7. 结果处理（保持原有 DB 回查 + ES 清理逻辑不变）
        Page<Article> page = new Page<>();
        page.setTotalRow(searchHits.getTotalHits());
        List<Article> resourceList = new ArrayList<>();

        if (searchHits.hasSearchHits()) {
            Map<Long, String> highlightMap = new HashMap<>();
            List<Long> articleIdList = searchHits.getSearchHits().stream()
                    .peek(hit -> {
                        Map<String, List<String>> highlightFields = hit.getHighlightFields();
                        if (highlightFields.containsKey("title") && !highlightFields.get("title").isEmpty()) {
                            highlightMap.put(hit.getContent().getId(), highlightFields.get("title").getFirst());
                        }
                    })
                    .map(hit -> hit.getContent().getId())
                    .collect(Collectors.toList());

            List<Article> articleList = this.listByIds(articleIdList);
            if (articleList != null) {
                Map<Long, List<Article>> idArticleMap = articleList.stream()
                        .collect(Collectors.groupingBy(Article::getId));
                // 遍历 ID 构建结果
                articleIdList.forEach(articleId -> {
                    if (idArticleMap.containsKey(articleId)) {
                        Article article = idArticleMap.get(articleId).getFirst();
                        // 设置高亮（如果存在）
                        if (highlightMap.containsKey(articleId)) {
                            article.setHighlightTitle(highlightMap.get(articleId));
                        }
                        // 添加已设置高亮的对象
                        resourceList.add(idArticleMap.get(articleId).getFirst());
                    } else {
                        elasticsearchOperations.delete(String.valueOf(articleId), ArticleEsDTO.class);
                        log.info("delete article(id:{}) from es (db already removed)", articleId);
                    }
                });
            }
        }
        // 2. 拼写纠错提取
        Suggest suggest = searchHits.getSuggest();
        if (suggest != null) {
            var suggestion = suggest.getSuggestion("title-suggest");
            for (var entry : suggestion.getEntries()) {
                log.debug("用户输入: {}", entry.getText());
                for (var option : entry.getOptions()) {
                    log.debug("→ 您是不是要找: {} (得分: {})", option.getText(), option.getScore());
                }
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public Page<ArticleVO> getArticleVOPage(Page<Article> articlePage) {
        List<Article> articleList = articlePage.getRecords();
        Page<ArticleVO> ArticleVOPage = new Page<>(articlePage.getPageNumber(), articlePage.getPageSize(), articlePage.getTotalRow());
        if (CollectionUtils.isEmpty(articleList)) {
            return ArticleVOPage;
        }
        // 填充信息
        List<ArticleVO> ArticleVOList = articleList.stream().map(ArticleVO::objToVo).toList();
        ArticleVOPage.setRecords(ArticleVOList);
        return ArticleVOPage;
    }
}
