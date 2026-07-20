package com.chipswu.aggregatedsearchplatform.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息视图类
 *
 * @author WuJiaJun
 */
@Data
public class UserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 733281271393277712L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 用户简介
     */
    private String profile;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
