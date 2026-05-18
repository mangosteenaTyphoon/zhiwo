package com.shanzhu.controller;

import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.dto.ShanzhuReviewQueryDTO;
import com.shanzhu.model.vo.ShanzhuReviewVO;
import com.shanzhu.service.ShanzhuReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "山竹复盘分析")
@RestController
@RequestMapping("shanzhu/review")
@Validated
public class ShanzhuReviewController extends ApiResponseController {

    @Resource
    private ShanzhuReviewService shanzhuReviewService;

    @Operation(summary = "查询复盘分析报告")
    @PostMapping("summary")
    public ApiResponseModel<ShanzhuReviewVO> summary(@RequestBody ShanzhuReviewQueryDTO queryDTO) {
        return success(shanzhuReviewService.queryReview(queryDTO));
    }
}
