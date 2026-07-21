package com.chipswu.aggregatedsearchplatform.datasource;

import com.chipswu.aggregatedsearchplatform.exception.BusinessException;
import com.chipswu.aggregatedsearchplatform.exception.ErrorCode;
import com.chipswu.aggregatedsearchplatform.model.dto.ArticleQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.dto.UserQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.entity.Picture;
import com.chipswu.aggregatedsearchplatform.model.enums.SearchTypeEnum;
import com.chipswu.aggregatedsearchplatform.model.vo.ArticleVO;
import com.chipswu.aggregatedsearchplatform.model.vo.SearchVO;
import com.chipswu.aggregatedsearchplatform.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author WuJiaJun
 */
@Slf4j
@Service
public class AllDataSource implements DataSource<SearchVO>{

    @Resource
    private ArticleDataSource articleDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    /**
     * 搜索
     *
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<SearchVO> doSearch(String searchText, int pageNum, int pageSize) {
        CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
            UserQueryRequest userQueryRequest = new UserQueryRequest();
            userQueryRequest.setUsername(searchText);
            return userDataSource.doSearch(searchText, pageNum, pageSize);
        });

        CompletableFuture<Page<ArticleVO>> postTask = CompletableFuture.supplyAsync(() -> {
            ArticleQueryRequest articleQueryRequest = new ArticleQueryRequest();
            articleQueryRequest.setSearchText(searchText);
            return articleDataSource.doSearch(searchText, pageNum, pageSize);
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
            Page<SearchVO> searchVOPage = new Page<>();
            searchVOPage.setRecords(List.of(searchVO));
            return searchVOPage;
        } catch (Exception e) {
            log.error("查询异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
        }
    }

    @Override
    public String getType() {
        return SearchTypeEnum.ALL.getValue();
    }
}
