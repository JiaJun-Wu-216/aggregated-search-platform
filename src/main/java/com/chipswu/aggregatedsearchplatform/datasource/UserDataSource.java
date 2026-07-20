package com.chipswu.aggregatedsearchplatform.datasource;


import com.chipswu.aggregatedsearchplatform.model.dto.UserQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.enums.SearchTypeEnum;
import com.chipswu.aggregatedsearchplatform.model.vo.UserVO;
import com.chipswu.aggregatedsearchplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 *
 * @author WuJiaJun
 */
@Service
@Slf4j
public class UserDataSource implements DataSource<UserVO> {

    @Resource
    private UserService userService;

    /**
     * 搜索
     *
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public Page<UserVO> doSearch(String searchText, int pageNum, int pageSize) {
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setUsername(searchText);
        userQueryRequest.setPageNum(pageNum);
        userQueryRequest.setPageSize(pageSize);
        return userService.listUserVOByPage(userQueryRequest);
    }

    @Override
    public String getType() {
        return SearchTypeEnum.USER.getValue();
    }
}