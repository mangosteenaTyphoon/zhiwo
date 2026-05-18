package com.shanzhu.controller;

import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.dto.ShanzhuCategorySaveDTO;
import com.shanzhu.model.vo.ShanzhuCategoryVO;
import com.shanzhu.service.ShanzhuCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "山竹分类管理")
@RestController
@RequestMapping("shanzhu/category")
@Validated
public class ShanzhuCategoryController extends ApiResponseController {

    @Resource
    private ShanzhuCategoryService shanzhuCategoryService;

    @Operation(summary = "查询分类列表")
    @PostMapping("list")
    public ApiResponseModel<List<ShanzhuCategoryVO>> list() {
        return success(shanzhuCategoryService.queryList());
    }

    @Operation(summary = "新增分类")
    @PostMapping("add")
    public ApiResponseModel<String> add(@RequestBody ShanzhuCategorySaveDTO saveDTO) {
        return success(shanzhuCategoryService.addCategory(saveDTO));
    }
}
