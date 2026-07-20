package com.chipswu.aggregatedsearchplatform.model.dto;

import com.chipswu.aggregatedsearchplatform.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 查询请求类
 *
 * @author WuJiaJun
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1044639835713056452L;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 类型
     */
    private String type;
}
