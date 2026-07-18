package com.chipswu.aggregatedsearchplatform.domain.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 图片实体类
 *
 * @author WuJiaJun
 */
@Data
@Table(value = "pictures")
public class Picture implements Serializable {

    @Serial
    private static final long serialVersionUID = -7736727466700271031L;

    /**
     * 图片主键
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Integer id;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片链接
     */
    private String url;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}