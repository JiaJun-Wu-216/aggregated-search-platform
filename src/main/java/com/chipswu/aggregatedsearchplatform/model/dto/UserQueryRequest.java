package com.chipswu.aggregatedsearchplatform.model.dto;

import com.chipswu.aggregatedsearchplatform.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author WuJiaJun
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7880731853627490635L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 简介
     */
    private String userProfile;

}