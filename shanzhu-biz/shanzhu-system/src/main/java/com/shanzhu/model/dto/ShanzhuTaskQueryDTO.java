package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShanzhuTaskQueryDTO {
    /**
     * 所属目标ID
     */
    @NotBlank(message = "请选择目标")
    private String goalId;

    /**
     * 所属子目标ID，可为空
     */
    private String subGoalId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 计划执行日期
     */
    private LocalDate plannedDate;
}
