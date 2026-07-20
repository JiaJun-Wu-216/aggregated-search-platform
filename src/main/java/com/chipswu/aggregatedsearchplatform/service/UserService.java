package com.chipswu.aggregatedsearchplatform.service;

import com.chipswu.aggregatedsearchplatform.model.dto.UserQueryRequest;
import com.chipswu.aggregatedsearchplatform.model.entity.User;
import com.chipswu.aggregatedsearchplatform.model.vo.UserVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 用户相关应用层接口
 *
 * @author WuJiaJun
 */
public interface UserService extends IService<User> {

    Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);

    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    UserVO getUserVO(User user);

    List<UserVO> getUserVO(List<User> userList);
}
