package com.shanzhu.controller;

import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.dto.ShanzhuSubGoalProgressDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalQueryDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalSaveDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalSortDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalStatusDTO;
import com.shanzhu.model.vo.ShanzhuSubGoalVO;
import com.shanzhu.service.ShanzhuSubGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "山竹子目标管理")
@RestController
@RequestMapping("shanzhu/sub-goal")
@Validated
public class ShanzhuSubGoalController extends ApiResponseController {

    @Resource
    private ShanzhuSubGoalService shanzhuSubGoalService;

    @Operation(summary = "查询目标下的子目标列表")
    @PostMapping("list")
    public ApiResponseModel<List<ShanzhuSubGoalVO>> list(@RequestBody @Validated ShanzhuSubGoalQueryDTO queryDTO) {
        return success(shanzhuSubGoalService.queryByGoalId(queryDTO.getGoalId()));
    }

    @Operation(summary = "保存子目标")
    @PostMapping
    public ApiResponseModel<String> save(@RequestBody @Validated ShanzhuSubGoalSaveDTO saveDTO) {
        return success(shanzhuSubGoalService.saveSubGoal(saveDTO));
    }

    @Operation(summary = "删除子目标")
    @DeleteMapping("{id}")
    public ApiResponseModel<String> delete(@PathVariable("id") @NotBlank(message = "请选择子目标") String id) {
        shanzhuSubGoalService.deleteSubGoal(id);
        return success();
    }

    @Operation(summary = "更新子目标状态")
    @PostMapping("status")
    public ApiResponseModel<String> updateStatus(@RequestBody @Validated ShanzhuSubGoalStatusDTO statusDTO) {
        shanzhuSubGoalService.updateStatus(statusDTO);
        return success();
    }

    @Operation(summary = "更新子目标进度")
    @PostMapping("progress")
    public ApiResponseModel<String> updateProgress(@RequestBody @Validated ShanzhuSubGoalProgressDTO progressDTO) {
        shanzhuSubGoalService.updateProgress(progressDTO);
        return success();
    }

    @Operation(summary = "调整子目标排序")
    @PostMapping("sort")
    public ApiResponseModel<String> updateSort(@RequestBody @Validated ShanzhuSubGoalSortDTO sortDTO) {
        shanzhuSubGoalService.updateSort(sortDTO);
        return success();
    }
}
