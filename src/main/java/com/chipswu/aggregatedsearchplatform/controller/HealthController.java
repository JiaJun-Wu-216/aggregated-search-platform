package com.chipswu.aggregatedsearchplatform.controller;

import com.chipswu.aggregatedsearchplatform.common.BaseResponse;
import com.chipswu.aggregatedsearchplatform.utils.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author WuJiaJun
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtils.success("健康");
    }
}
