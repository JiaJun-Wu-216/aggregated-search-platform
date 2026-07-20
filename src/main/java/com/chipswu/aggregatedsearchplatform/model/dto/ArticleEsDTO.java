package com.chipswu.aggregatedsearchplatform.model.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章 ES 包装类
 *
 * @author WuJiaJun
 **/
@Document(indexName = "articles")
@Data
public class ArticleEsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8030449795078213325L;

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ssXXX";

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * 标题
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    /**
     * 创建时间
     */
    @Field(name = "create_time", index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Field(name = "update_time", index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private LocalDateTime updateTime;

    /**
     * 用于搜索建议的 completion 字段
     */
    /*@CompletionField(maxInputLength = 100)
    private Completion suggest; // 存储标题/关键词的拼音或前缀*/
}