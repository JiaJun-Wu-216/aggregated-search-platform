package com.chipswu.aggregatedsearchplatform.manager;

import com.chipswu.aggregatedsearchplatform.datasource.DataSource;
import com.chipswu.aggregatedsearchplatform.datasource.DataSourceRegistry;
import com.chipswu.aggregatedsearchplatform.exception.ErrorCode;
import com.chipswu.aggregatedsearchplatform.exception.ThrowUtils;
import com.chipswu.aggregatedsearchplatform.model.dto.SearchRequest;
import com.chipswu.aggregatedsearchplatform.model.enums.SearchTypeEnum;
import com.chipswu.aggregatedsearchplatform.model.vo.SearchVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
