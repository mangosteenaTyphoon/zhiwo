package com.shanzhu.controller;

import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "山竹目标进展管理")
@RestController
@RequestMapping("shanzhu/goal-progress")
@Validated
public class ShanzhuGoalProgressController extends ApiResponseController {
}
