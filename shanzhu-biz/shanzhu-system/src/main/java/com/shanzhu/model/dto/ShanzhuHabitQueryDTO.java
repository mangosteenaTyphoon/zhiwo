package com.shanzhu.model.dto;

import lombok.Data;

@Data
public class ShanzhuHabitQueryDTO {
    /**
     * 关键词
     */
    private String keyword;

    /**
     * 关联目标ID
     */
    private String goalId;

    /**
     * 关联子目标ID
     */
    private String subGoalId;

    /**
     * 习惯状态
     */
    private String status;

    /**
     * 频率类型
     */
    private String frequencyType;

    /**
     * 当前页数
     */
    private Long pageNum;

    /**
     * 每页记录数
     */
    private Long pageSize;
}
