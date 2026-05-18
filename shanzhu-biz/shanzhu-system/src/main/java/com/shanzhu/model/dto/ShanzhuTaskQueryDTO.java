package com.shanzhu.model.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ShanzhuTaskQueryDTO {
    /**
     * 关键词
     */
    private String keyword;

    /**
     * 所属目标ID
     */
    private String goalId;

    /**
     * 目标分类ID
     */
    private String categoryId;

    /**
     * 标签ID列表
     */
    private List<String> tagIds;

    /**
     * 所属子目标ID，可为空
     */
    private String subGoalId;

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
     * 查询类型：today 今日任务，overdue 逾期任务
     */
    private String queryType;
}
