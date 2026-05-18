package com.shanzhu.controller;

import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.vo.ShanzhuDashboardVO;
import com.shanzhu.service.ShanzhuDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "山竹统计仪表盘")
@RestController
@RequestMapping("shanzhu/dashboard")
@Validated
public class ShanzhuDashboardController extends ApiResponseController {

    @Resource
    private ShanzhuDashboardService shanzhuDashboardService;

    @Operation(summary = "查询目标任务仪表盘统计")
    @GetMapping
    public ApiResponseModel<ShanzhuDashboardVO> dashboard() {
        return success(shanzhuDashboardService.queryDashboard());
    }
}
