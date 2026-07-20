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

    public Article() {
    }

    public Article(Long id, String title, String author, String avatar, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.avatar = avatar;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Article(Long id, String title, String author, String avatar, LocalDateTime createTime, LocalDateTime updateTime, String highlightTitle) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.avatar = avatar;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.highlightTitle = highlightTitle;
    }

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

    @Column(ignore = true)
    private String highlightTitle;
}