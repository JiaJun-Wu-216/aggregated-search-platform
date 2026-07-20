package com.chipswu.aggregatedsearchplatform.service.impl;

import com.chipswu.aggregatedsearchplatform.constant.CommonConstant;
import com.chipswu.aggregatedsearchplatform.exception.BusinessException;
import com.chipswu.aggregatedsearchplatform.exception.ErrorCode;
import com.chipswu.aggregatedsearchplatform.mapper.UserMapper;
import com.chipswu.aggregatedsearchplatform.model.dto.UserQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.entity.User;
import com.chipswu.aggregatedsearchplatform.model.vo.UserVO;
import com.chipswu.aggregatedsearchplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.chipswu.aggregatedsearchplatform.model.entity.table.UserTableDef.USER;

/**
 * 用户相关应用层接口实现类
 *
 * @author WuJiaJun
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
        int currentPage = userQueryRequest.getPageNum();
        int size = userQueryRequest.getPageSize();
        Page<User> userPage = this.page(new Page<>(currentPage, size),
                this.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(currentPage, size, userPage.getTotalRow());
        List<UserVO> userVO = this.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return userVOPage;
    }

    @Override
    public QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest){
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String username = userQueryRequest.getUsername();
        String userProfile = userQueryRequest.getUserProfile();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        return this.query()
                .where(USER.ID.eq(id, id != null))
                .and(USER.USERNAME.like(username).when(StringUtils.isNotBlank(username)))
                .and(USER.PROFILE.like(userProfile).when(StringUtils.isNotBlank(userProfile)))
                .orderBy(sortField, sortOrder.equals(CommonConstant.SORT_ORDER_ASC));
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }
}
