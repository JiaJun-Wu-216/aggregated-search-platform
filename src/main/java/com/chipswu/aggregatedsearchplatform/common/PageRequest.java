package com.chipswu.aggregatedsearchplatform.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页请求类
 *
 * @author WuJiaJun
 */
@Data
public class PageRequest {

    /**
     * 当前页码
     */
    @Min(value = 1, message = "【当前页码】最小值只能为 1")
    private int pageNum = 1;

    /**
     * 每页展示数量
     */
    @Max(value = 20, message = "【每页展示数量】最大值为 20")
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}
