package com.shanzhu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.dto.ShanzhuTodoQueryDTO;
import com.shanzhu.model.dto.ShanzhuTodoSaveDTO;
import com.shanzhu.model.vo.ShanzhuTodoVO;
import com.shanzhu.service.ShanzhuTodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "山竹收集箱")
@RestController
@RequestMapping("shanzhu/todo")
@Validated
public class ShanzhuTodoController extends ApiResponseController {

    @Resource
    private ShanzhuTodoService shanzhuTodoService;

    @Operation(summary = "分页查询Todo列表")
    @PostMapping("page")
    public ApiResponseModel<IPage<ShanzhuTodoVO>> page(@RequestBody @Validated ShanzhuTodoQueryDTO queryDTO) {
        return success(shanzhuTodoService.queryTodoPage(queryDTO));
    }

    @Operation(summary = "查询Todo列表")
    @PostMapping("list")
    public ApiResponseModel<List<ShanzhuTodoVO>> list(@RequestBody @Validated ShanzhuTodoQueryDTO queryDTO) {
        return success(shanzhuTodoService.queryTodoList(queryDTO));
    }

    @Operation(summary = "保存Todo")
    @PostMapping
    public ApiResponseModel<String> save(@RequestBody @Validated ShanzhuTodoSaveDTO saveDTO) {
        return success(shanzhuTodoService.saveTodo(saveDTO));
    }

    @Operation(summary = "删除Todo")
    @DeleteMapping("{id}")
    public ApiResponseModel<String> delete(@PathVariable("id") @NotBlank(message = "请选择Todo") String id) {
        shanzhuTodoService.deleteTodo(id);
        return success();
    }

    @Operation(summary = "完成Todo")
    @PostMapping("{id}/complete")
    public ApiResponseModel<String> complete(@PathVariable("id") @NotBlank(message = "请选择Todo") String id) {
        shanzhuTodoService.completeTodo(id);
        return success();
    }

    @Operation(summary = "取消完成Todo")
    @PostMapping("{id}/uncomplete")
    public ApiResponseModel<String> uncomplete(@PathVariable("id") @NotBlank(message = "请选择Todo") String id) {
        shanzhuTodoService.uncompleteTodo(id);
        return success();
    }

    @Operation(summary = "移入今日待办")
    @PostMapping("{id}/today")
    public ApiResponseModel<String> moveToToday(@PathVariable("id") @NotBlank(message = "请选择Todo") String id) {
        shanzhuTodoService.moveToToday(id);
        return success();
    }

    @Operation(summary = "移回收集箱")
    @PostMapping("{id}/inbox")
    public ApiResponseModel<String> moveToInbox(@PathVariable("id") @NotBlank(message = "请选择Todo") String id) {
        shanzhuTodoService.moveToInbox(id);
        return success();
    }

    @Operation(summary = "归档Todo")
    @PostMapping("{id}/archive")
    public ApiResponseModel<String> archive(@PathVariable("id") @NotBlank(message = "请选择Todo") String id) {
        shanzhuTodoService.archiveTodo(id);
        return success();
    }
}
