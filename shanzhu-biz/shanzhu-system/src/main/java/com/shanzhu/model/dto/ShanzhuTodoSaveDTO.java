package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ShanzhuTodoSaveDTO {
    /**
     * TodoID，为空时新增
     */
    private String id;

    /**
     * 关联目标ID，可为空
     */
    private String goalId;

    /**
     * 关联子目标ID，可为空
     */
    private String subGoalId;

    /**
     * Todo标题
     */
    @NotBlank(message = "请输入标题")
    private String title;

    /**
     * Todo说明
     */
    private String description;

    /**
     * 状态：inbox 收集箱, today 今日待办, done 已完成, archived 已归档, converted 已转任务
     */
    private String status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 计划处理日期
     */
    private LocalDate plannedDate;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 预计耗时，单位分钟
     */
    private Integer estimatedMinutes;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}