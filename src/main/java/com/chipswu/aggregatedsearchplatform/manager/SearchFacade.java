package com.chipswu.aggregatedsearchplatform.manager;

import com.chipswu.aggregatedsearchplatform.datasource.*;
import com.chipswu.aggregatedsearchplatform.exception.BusinessException;
import com.chipswu.aggregatedsearchplatform.exception.ErrorCode;
import com.chipswu.aggregatedsearchplatform.exception.ThrowUtils;
import com.chipswu.aggregatedsearchplatform.model.dto.ArticleQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.dto.SearchRequest;
import com.chipswu.aggregatedsearchplatform.model.dto.UserQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.entity.Picture;
import com.chipswu.aggregatedsearchplatform.model.enums.SearchTypeEnum;
import com.chipswu.aggregatedsearchplatform.model.vo.ArticleVO;
import com.chipswu.aggregatedsearchplatform.model.vo.SearchVO;
import com.chipswu.aggregatedsearchplatform.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 *
 * @author WuJiaJun
 */
@Slf4j
@Component
public class SearchFacade {

    @Resource
    private ArticleDataSource articleDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private DataSourceRegistry dataSourceRegistry;

    public SearchVO searchAll(SearchRequest searchRequest) {
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR);
        String searchText = searchRequest.getSearchText();
        int currentPage = searchRequest.getPageNum();
        int pageSize = searchRequest.getPageSize();
        // 搜索出所有数据
        if (searchTypeEnum == null) {
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                UserQueryRequest userQueryRequest = new UserQueryRequest();
                userQueryRequest.setUsername(searchText);
                return userDataSource.doSearch(searchText, currentPage, pageSize);
            });

            CompletableFuture<Page<ArticleVO>> postTask = CompletableFuture.supplyAsync(() -> {
                ArticleQueryRequest articleQueryRequest = new ArticleQueryRequest();
                articleQueryRequest.setSearchText(searchText);
                return articleDataSource.doSearch(searchText, currentPage, pageSize);
            });

            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> pictureDataSource.doSearch(searchText, 1, 10));

            CompletableFuture.allOf(userTask, postTask, pictureTask).join();

            try {
                Page<UserVO> userVOPage = userTask.get();
                Page<ArticleVO> postVOPage = postTask.get();
                Page<Picture> picturePage = pictureTask.get();
                SearchVO searchVO = new SearchVO();
                searchVO.setUserList(userVOPage.getRecords());
                searchVO.setPostList(postVOPage.getRecords());
                searchVO.setPictureList(picturePage.getRecords());
                return searchVO;
            } catch (Exception e) {
                log.error("查询异常", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
            }
        } else {
            SearchVO searchVO = new SearchVO();
            DataSource<?> dataSource = dataSourceRegistry.getDataSourceByType(type);
            Page<?> page = dataSource.doSearch(searchText, currentPage, pageSize);
            searchVO.setDataList(page.getRecords());
            return searchVO;
        }
    }
}
