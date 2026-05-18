package com.shanzhu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.dto.ShanzhuTaskQueryDTO;
import com.shanzhu.model.dto.ShanzhuTaskSaveDTO;
import com.shanzhu.model.dto.ShanzhuTaskSortDTO;
import com.shanzhu.model.dto.ShanzhuTaskStatusDTO;
import com.shanzhu.model.vo.ShanzhuTaskVO;
import com.shanzhu.service.ShanzhuTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "山竹任务管理")
@RestController
@RequestMapping("shanzhu/task")
@Validated
public class ShanzhuTaskController extends ApiResponseController {

    @Resource
    private ShanzhuTaskService shanzhuTaskService;

    @Operation(summary = "分页查询任务列表")
    @PostMapping("page")
    public ApiResponseModel<IPage<ShanzhuTaskVO>> page(@RequestBody @Validated ShanzhuTaskQueryDTO queryDTO) {
        return success(shanzhuTaskService.queryTaskPage(queryDTO));
    }

    @Operation(summary = "查询目标下的任务列表")
    @PostMapping("list")
    public ApiResponseModel<List<ShanzhuTaskVO>> list(@RequestBody @Validated ShanzhuTaskQueryDTO queryDTO) {
        return success(shanzhuTaskService.queryTaskList(queryDTO));
    }

    @Operation(summary = "保存任务")
    @PostMapping
    public ApiResponseModel<String> save(@RequestBody @Validated ShanzhuTaskSaveDTO saveDTO) {
        return success(shanzhuTaskService.saveTask(saveDTO));
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("{id}")
    public ApiResponseModel<String> delete(@PathVariable("id") @NotBlank(message = "请选择任务") String id) {
        shanzhuTaskService.deleteTask(id);
        return success();
    }

    @Operation(summary = "更新任务状态")
    @PostMapping("status")
    public ApiResponseModel<String> updateStatus(@RequestBody @Validated ShanzhuTaskStatusDTO statusDTO) {
        shanzhuTaskService.updateStatus(statusDTO);
        return success();
    }

    @Operation(summary = "调整任务排序")
    @PostMapping("sort")
    public ApiResponseModel<String> updateSort(@RequestBody @Validated ShanzhuTaskSortDTO sortDTO) {
        shanzhuTaskService.updateSort(sortDTO);
        return success();
    }
}
