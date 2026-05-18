package com.shanzhu.controller;

import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.dto.ShanzhuGoalProgressQueryDTO;
import com.shanzhu.model.dto.ShanzhuGoalProgressSaveDTO;
import com.shanzhu.model.vo.ShanzhuGoalProgressVO;
import com.shanzhu.service.ShanzhuGoalProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "山竹目标进展管理")
@RestController
@RequestMapping("shanzhu/goal-progress")
@Validated
public class ShanzhuGoalProgressController extends ApiResponseController {

    @Resource
    private ShanzhuGoalProgressService shanzhuGoalProgressService;

    @Operation(summary = "查询目标进展记录")
    @PostMapping("list")
    public ApiResponseModel<List<ShanzhuGoalProgressVO>> list(@RequestBody @Validated ShanzhuGoalProgressQueryDTO queryDTO) {
        return success(shanzhuGoalProgressService.queryByGoalId(queryDTO));
    }

    @Operation(summary = "保存目标进展记录")
    @PostMapping
    public ApiResponseModel<String> save(@RequestBody @Validated ShanzhuGoalProgressSaveDTO saveDTO) {
        return success(shanzhuGoalProgressService.saveProgress(saveDTO));
    }

    @Operation(summary = "删除目标进展记录")
    @DeleteMapping("{id}")
    public ApiResponseModel<String> delete(@PathVariable("id") @NotBlank(message = "请选择进展记录") String id) {
        shanzhuGoalProgressService.deleteProgress(id);
        return success();
    }
}
