# 项目介绍

基于 Vue 3 + Spring Boot + Elastic Stack 的一站式聚合搜索平台，也是简化版的企业级搜索中台。

对用户来说，使用该平台，可以在同一个页面集中搜索出不同来源、不同类型的内容，提升用户的检索效率和搜索体验。

对企业来说，当企业内部有多个项目的数据都存在搜索需求时，无需针对每个项目单独开发搜索功能，可以直接将各项目的数据源接入搜索中台，从而提升开发效率、降低系统维护成本。

- 聚合搜索页面-搜文章

![image-20260721125838617](E:\Project\aggregated-search-platform\assets\image-20260721125838617.png)

- 聚合搜索页面-搜图片

![image-20260721130029900](E:\Project\aggregated-search-platform\assets\image-20260721130029900.png)

- 聚合搜索页面-搜用户

![image-20260721130125991](E:\Project\aggregated-search-platform\assets\image-20260721130125991.png)

- Elastic Stack - Kibana 数据可视化

![image-20260721131048028](E:\Project\aggregated-search-platform\assets\image-20260721131048028.png)

- 项目架构图

![image-20260721131249817](E:\Project\aggregated-search-platform\assets\image-20260721131249817.png)

# 技术选型（全栈项目）
## 前端
- Vue 3
- Ant Design Vue 组件库
- 页面状态同步
## 后端
- JDK 21
- Spring Boot 3.5.16
- MySQL 数据库（8.x 版本）
- Elastic Stack
  - Elasticsearch 搜索引擎（重点）
  - Logstash 数据管道
  - Kibana 数据可视化
- 数据抓取（jsoup、HttpClient 爬虫）
  - 离线
  - 实时
- 设计模式
  - 门面模式
  - 适配器模式
  - 注册器模式
- 数据同步（4 种同步方式）
  - 定时
  - 双写
  - Logstash
  - Cloud Canal
- JMeter 压力测试
- resilience4j-retry（resilience4j - 重试模块）

# 亮点展示

## 策略设计模式

![image-20260721132610663](E:\Project\aggregated-search-platform\assets\image-20260721132610663.png)

注册器模式的主要目的是在应用程序中全局注册一些对象，便于被其他对象发现和使用，常用于管理和维护一组单例的全局对象。
使用注册器模式后，不仅能更方便地集中查找和获取全局对象，还避免了反复初始化的内存和时间开销。
具体的实现方式如下：

1. 定义注册器类：将注册器类标识为一个 Bean，并且在类中添加一个 HashMap 属性，用于存放全局对象。
2. 对象注册：通过 @PostConstruct 注解，在注册器 Bean 加载时初始化 HashMap 并且向其中插入新创建的数据源对象。
3. 对象获取：提供一个根据 key（数据源类型）查找对象的方法，返回获取到的对象。

```Java
/**
 * 数据源注册器
 */
@Slf4j
@Component
public class DataSourceRegistry {

    // 1. 注入所有 DataSource 实现类，无需手动逐个 @Resource
    private final List<DataSource<?>> dataSources;

    // 2. 使用不可变 Map，key 为 String，value 为通配符泛型
    private Map<String, DataSource<?>> typeDataSourceMap;

    // 3. 通过构造器注入，更符合 Spring 最佳实践且利于单测
    public DataSourceRegistry(List<DataSource<?>> dataSources) {
        this.dataSources = dataSources;
    }

    @PostConstruct
    public void doInit() {
        // 4. 自动收集并构建不可变 Map，彻底消除双括号初始化
        Map<String, DataSource<?>> map = dataSources.stream()
                .collect(Collectors.toMap(
                        DataSource::getType,
                        Function.identity(),
                        (existing, replacement) -> {
                            log.error("检测到重复的数据源类型: {}, 后者将覆盖前者", existing);
                            return replacement;
                        }
                ));

        this.typeDataSourceMap = Collections.unmodifiableMap(map);
        log.info("数据源注册完成，已加载 {} 个数据源: {}", map.size(), map.keySet());
    }

    /**
     * 根据类型获取数据源
     * @param type 搜索类型字符串
     * @return 对应的数据源，未找到返回 null
     */
    public DataSource<?> getDataSourceByType(String type) {
        // 5. @PostConstruct 后 map 绝不为 null，移除冗余判空
        DataSource<?> dataSource = typeDataSourceMap.get(type);
        if (dataSource == null) {
            log.warn("未找到对应类型的数据源, type={}", type);
        }
        return dataSource;
    }
}
```

```java
/**
 * 搜索门面
 *
 * @author WuJiaJun
 */
@Slf4j
@Component
public class SearchFacade {

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    public SearchVO searchAll(SearchRequest searchRequest) {
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        ThrowUtils.throwIf(searchTypeEnum == null, ErrorCode.PARAMS_ERROR);
        // 搜索出所有数据
        SearchVO searchVO = new SearchVO();
        DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(searchTypeEnum.getValue());
        Page<?> page = dataSource.doSearch(
                searchRequest.getSearchText(),
                searchRequest.getPageNum(),
                searchRequest.getPageSize());
        if (SearchTypeEnum.ALL.getValue().equals(searchTypeEnum.getValue())) {
            return (SearchVO) page.getRecords().getFirst();
        } else {
            searchVO.setDataList(page.getRecords());
            return searchVO;
        }
    }
}
```

通过判断前端传递的搜索类型，选择对应的数据搜索操作

## 适配器模式

配器模式的主要目的是将一个类的接口转换成客户端所期望的另一个接口，使得原本由于接口不兼容而不能一起工作的类可以协同工作，就像是手机充电器的转接头一样.
适配器模式的主要作用：
1. 接口转换：适配器模式允许将一个类的接口转换成另一个类所期望的接口，使得两个类可以协同工作，而无需修改它们的源代码。
2. 解耦合：适配器模式可以帮助解耦不合兼容的接口，使得客户端与适配器之间的接口保持一致，降低了代码的依赖性。
3. 复用性：适配器模式可以将现有的类用于新的应用场景，增加了代码的复用性。
在本项目中，定制了统一的数据源接入规范（数据源接口），比如任何接入聚合搜索系统的数据，必须要能够根据关键词搜索、并且支持分页搜索。
在这个前提下，对于有些想要接入我们系统的数据源，如果原有的搜索方法参数和我们接口的定义不一致，又不能改造我们的接口以及对方原本的搜索方法，这时就需要使用适配器模式。
比如搜索文章数据源，有一个现成的 searchFromEs 的方法，但接受的搜索参数是一个对象而不是 searchText 字符串，就需要使用适配器模式进行转换，对请求参数进行封装。
示例代码如下：

```Java
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
```

## 第三方接口重试

```Java
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
```

使用 resilience4j-retry 保障在调用第三方接口时的稳定与降级

## 使用 Cloud Canal 进行数据同步

![image-20260721162810482](E:\Project\aggregated-search-platform\assets\image-20260721162810482.png)

将 MySQL 的 aggregated-search-platform.articles 表内容同步至 ElasticSearch 的 articles_es 索引中

## 搜索文章时使用搜索关键词高亮和搜索建议

```Java
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
```