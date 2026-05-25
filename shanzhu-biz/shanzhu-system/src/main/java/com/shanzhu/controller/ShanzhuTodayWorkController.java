package com.shanzhu.controller;

import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.vo.ShanzhuTodayWorkVO;
import com.shanzhu.service.ShanzhuTodayWorkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "山竹今日工作台")
@RestController
@RequestMapping("shanzhu/today-work")
public class ShanzhuTodayWorkController extends ApiResponseController {

    @Resource
    private ShanzhuTodayWorkService shanzhuTodayWorkService;

    @Operation(summary = "查询今日工作台数据")
    @GetMapping
    public ApiResponseModel<ShanzhuTodayWorkVO> queryTodayWork() {
        return success(shanzhuTodayWorkService.queryTodayWork());
    }
}
