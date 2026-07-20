package com.chipswu.aggregatedsearchplatform.model.dto;

import com.chipswu.aggregatedsearchplatform.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文章查询请求类
 *
 * @author WuJiaJun
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleQueryRequest extends PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -5020099063720063382L;

    /**
     * id
     */
    private Long id;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 标题
     */
    private String title;
}
