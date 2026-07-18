package com.chipswu.aggregatedsearchplatform.common;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用删除请求类
 *
 * @author WuJiaJun
 */
@Data
public class DeleteRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 3700277756121782207L;

    /**
     * 主键
     */
    @Min(value = 1, message = "主键参数异常")
    @NotNull(message = "主键不能为空")
    private Long id;

}
