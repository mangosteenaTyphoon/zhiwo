package com.shanzhu.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ShanzhuTodoQueryDTO {
    /**
     * 关键词
     */
    private String keyword;

    /**
     * 状态：inbox 收集箱, today 今日待办, done 已完成, archived 已归档, converted 已转任务
     */
    private String status;

    /**
     * 关联目标ID
     */
    private String goalId;

    /**
     * 关联子目标ID
     */
    private String subGoalId;

    /**
     * 计划处理日期
     */
    private LocalDate plannedDate;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 当前页数
     */
    private Long pageNum;

    /**
     * 每页记录数
     */
    private Long pageSize;
}
