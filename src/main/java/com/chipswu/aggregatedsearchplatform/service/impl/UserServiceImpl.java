package com.chipswu.aggregatedsearchplatform.service.impl;

import com.chipswu.aggregatedsearchplatform.domain.entity.User;
import com.chipswu.aggregatedsearchplatform.mapper.UserMapper;
import com.chipswu.aggregatedsearchplatform.service.UserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户相关应用层接口实现类
 *
 * @author WuJiaJun
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
