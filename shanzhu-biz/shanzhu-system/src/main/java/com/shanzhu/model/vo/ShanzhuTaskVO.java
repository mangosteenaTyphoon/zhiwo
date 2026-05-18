package com.shanzhu.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ShanzhuTaskVO {
    /**
     * 任务ID
     */
    private String id;

    /**
     * 所属目标ID
     */
    private String goalId;

    /**
     * 所属子目标ID，可为空
     */
    private String subGoalId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 任务标题
     */
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
    private LocalDateTime deadline;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

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
     * 创建时间
     */
    private LocalDateTime createTime;
}
