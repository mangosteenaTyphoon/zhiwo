package com.shanzhu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.dto.ShanzhuGoalQueryDTO;
import com.shanzhu.model.dto.ShanzhuGoalSaveDTO;
import com.shanzhu.model.vo.ShanzhuGoalVO;
import com.shanzhu.mybatis.model.validation.MaxPageSizeLimit;
import com.shanzhu.service.ShanzhuGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "山竹目标管理")
@RestController
@RequestMapping("shanzhu/goal")
@Validated
public class ShanzhuGoalController extends ApiResponseController {

    @Resource
    private ShanzhuGoalService shanzhuGoalService;

    @Operation(summary = "分页查询目标列表")
    @PostMapping("page")
    public ApiResponseModel<IPage<ShanzhuGoalVO>> queryPage(@RequestBody @Validated(MaxPageSizeLimit.class) ShanzhuGoalQueryDTO queryDTO) {
        return success(shanzhuGoalService.queryPage(queryDTO));
    }

    @Operation(summary = "根据ID查询目标详情")
    @GetMapping("{id}")
    public ApiResponseModel<ShanzhuGoalVO> queryById(@PathVariable("id") String id) {
        return success(shanzhuGoalService.queryById(id));
    }

    @Operation(summary = "保存目标")
    @PostMapping
    public ApiResponseModel<String> save(@RequestBody @Validated ShanzhuGoalSaveDTO saveDTO) {
        return success(shanzhuGoalService.saveGoal(saveDTO));
    }

    @Operation(summary = "删除目标")
    @DeleteMapping("{id}")
    public ApiResponseModel<String> delete(@PathVariable("id") @NotBlank(message = "请选择目标") String id) {
        shanzhuGoalService.deleteGoal(id);
        return success();
    }
}
