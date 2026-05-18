package com.shanzhu.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShanzhuHabitTodayVO {
    /**
     * 习惯ID
     */
    private String id;

    /**
     * 习惯名称
     */
    private String title;

    /**
     * 每次目标值
     */
    private BigDecimal targetValue;

    /**
     * 目标单位
     */
    private String unit;

    /**
     * 今日是否已打卡
     */
    private Boolean todayChecked;

    /**
     * 今日打卡记录ID
     */
    private String todayCheckinId;

    /**
     * 今日实际完成值
     */
    private BigDecimal actualValue;

    /**
     * 今日打卡备注
     */
    private String note;

    /**
     * 关联目标ID
     */
    private String goalId;

    /**
     * 目标名称
     */
    private String goalTitle;

    /**
     * 关联子目标ID
     */
    private String subGoalId;

    /**
     * 子目标名称
     */
    private String subGoalTitle;
}
