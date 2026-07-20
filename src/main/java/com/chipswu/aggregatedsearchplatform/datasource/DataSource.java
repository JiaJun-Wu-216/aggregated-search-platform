package com.chipswu.aggregatedsearchplatform.datasource;


import com.mybatisflex.core.paginate.Page;

/**
 * 数据源接口（新接入的数据源必须实现）
 *
 * @author WuJiaJun
 */
public interface DataSource<T> {
    /**
     * 搜索
     *
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<T> doSearch(String searchText, int pageNum, int pageSize);

    String getType();
}
