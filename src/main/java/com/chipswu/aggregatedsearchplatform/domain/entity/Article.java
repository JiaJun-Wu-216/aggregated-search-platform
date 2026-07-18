package com.chipswu.aggregatedsearchplatform.domain.entity;

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
 * 文章实体类
 *
 * @author WuJiaJun
 */
@Builder
@Data
@Table(value = "articles")
public class Article implements Serializable {

    @Serial
    private static final long serialVersionUID = 47963581080514183L;

    /**
     * 文章主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 作者
     */
    private String author;

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