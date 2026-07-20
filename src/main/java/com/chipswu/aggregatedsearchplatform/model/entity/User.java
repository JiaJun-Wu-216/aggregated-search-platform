package com.chipswu.aggregatedsearchplatform.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author WuJiaJun
 */
@Builder
@Data
@Table(value = "users")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 5372566376122036582L;

    /**
     * 用户主键
     */
    @Id(keyType = KeyType.Generator,value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 个人简介
     */
    private String profile;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime updateTime;
}