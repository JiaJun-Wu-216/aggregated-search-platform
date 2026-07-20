package com.chipswu.aggregatedsearchplatform.controller;

import com.chipswu.aggregatedsearchplatform.common.BaseResponse;
import com.chipswu.aggregatedsearchplatform.manager.SearchFacade;
import com.chipswu.aggregatedsearchplatform.model.dto.SearchRequest;
import com.chipswu.aggregatedsearchplatform.model.vo.SearchVO;
import com.chipswu.aggregatedsearchplatform.utils.ResultUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 查询接口
 *
 * @author WuJiaJun
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private SearchFacade searchFacade;

    @PostMapping("/all")
    public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchRequest) {
        return ResultUtils.success(searchFacade.searchAll(searchRequest));
    }
}
