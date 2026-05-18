package com.shanzhu.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShanzhuTaskSaveDTO {
    /**
     * 任务ID，有值时更新任务
     */
    private String id;

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
     * 任务标题
     */
    @NotBlank(message = "请输入任务标题")
    private String title;

    /**
     * 任务说明
     */
    private String description;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 计划执行日期
     */
    private LocalDate plannedDate;

    /**
     * 截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;

    /**
     * 预计耗时，单位分钟
     */
    private Integer estimatedMinutes;

    /**
     * 实际耗时，单位分钟
     */
    private Integer actualMinutes;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 标签ID列表
     */
    private List<String> tagIds;
}
