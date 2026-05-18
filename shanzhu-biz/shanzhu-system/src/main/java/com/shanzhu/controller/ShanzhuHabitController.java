package com.shanzhu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.dto.ShanzhuHabitCheckinQueryDTO;
import com.shanzhu.model.dto.ShanzhuHabitCheckinSaveDTO;
import com.shanzhu.model.dto.ShanzhuHabitQueryDTO;
import com.shanzhu.model.dto.ShanzhuHabitSaveDTO;
import com.shanzhu.model.vo.ShanzhuHabitCheckinVO;
import com.shanzhu.model.vo.ShanzhuHabitStatsVO;
import com.shanzhu.model.vo.ShanzhuHabitTodayVO;
import com.shanzhu.model.vo.ShanzhuHabitVO;
import com.shanzhu.service.ShanzhuHabitCheckinService;
import com.shanzhu.service.ShanzhuHabitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "山竹习惯打卡")
@RestController
@RequestMapping("shanzhu/habit")
@Validated
public class ShanzhuHabitController extends ApiResponseController {

    @Resource
    private ShanzhuHabitService shanzhuHabitService;

    @Resource
    private ShanzhuHabitCheckinService shanzhuHabitCheckinService;

    @Operation(summary = "分页查询习惯列表")
    @PostMapping("page")
    public ApiResponseModel<IPage<ShanzhuHabitVO>> page(@RequestBody @Validated ShanzhuHabitQueryDTO queryDTO) {
        return success(shanzhuHabitService.queryHabitPage(queryDTO));
    }

    @Operation(summary = "查询习惯列表")
    @PostMapping("list")
    public ApiResponseModel<List<ShanzhuHabitVO>> list(@RequestBody @Validated ShanzhuHabitQueryDTO queryDTO) {
        return success(shanzhuHabitService.queryHabitList(queryDTO));
    }

    @Operation(summary = "保存习惯")
    @PostMapping
    public ApiResponseModel<String> save(@RequestBody @Validated ShanzhuHabitSaveDTO saveDTO) {
        return success(shanzhuHabitService.saveHabit(saveDTO));
    }

    @Operation(summary = "删除习惯")
    @DeleteMapping("{id}")
    public ApiResponseModel<String> delete(@PathVariable("id") @NotBlank(message = "请选择习惯") String id) {
        shanzhuHabitService.deleteHabit(id);
        return success();
    }

    @Operation(summary = "暂停习惯")
    @PostMapping("{id}/pause")
    public ApiResponseModel<String> pause(@PathVariable("id") @NotBlank(message = "请选择习惯") String id) {
        shanzhuHabitService.pauseHabit(id);
        return success();
    }

    @Operation(summary = "恢复习惯")
    @PostMapping("{id}/resume")
    public ApiResponseModel<String> resume(@PathVariable("id") @NotBlank(message = "请选择习惯") String id) {
        shanzhuHabitService.resumeHabit(id);
        return success();
    }

    @Operation(summary = "查询今日待打卡列表")
    @GetMapping("today")
    public ApiResponseModel<List<ShanzhuHabitTodayVO>> today() {
        return success(shanzhuHabitService.queryTodayHabits());
    }

    @Operation(summary = "查询习惯统计")
    @GetMapping("stats")
    public ApiResponseModel<ShanzhuHabitStatsVO> stats() {
        return success(shanzhuHabitService.queryHabitStats());
    }

    @Operation(summary = "打卡或修改打卡")
    @PostMapping("checkin")
    public ApiResponseModel<String> checkin(@RequestBody @Validated ShanzhuHabitCheckinSaveDTO saveDTO) {
        return success(shanzhuHabitCheckinService.saveCheckin(saveDTO));
    }

    @Operation(summary = "取消打卡")
    @DeleteMapping("checkin/{id}")
    public ApiResponseModel<String> cancelCheckin(@PathVariable("id") @NotBlank(message = "请选择打卡记录") String id) {
        shanzhuHabitCheckinService.cancelCheckin(id);
        return success();
    }

    @Operation(summary = "查询习惯打卡记录")
    @GetMapping("{id}/checkins")
    public ApiResponseModel<List<ShanzhuHabitCheckinVO>> checkins(
            @PathVariable("id") @NotBlank(message = "请选择习惯") String id,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        ShanzhuHabitCheckinQueryDTO queryDTO = new ShanzhuHabitCheckinQueryDTO();
        queryDTO.setHabitId(id);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        return success(shanzhuHabitCheckinService.queryCheckins(queryDTO));
    }
}
