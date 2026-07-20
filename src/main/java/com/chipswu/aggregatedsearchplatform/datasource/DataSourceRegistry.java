package com.chipswu.aggregatedsearchplatform.datasource;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 数据源注册器
 *
 * @author WuJiaJun
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