package com.shanzhu.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShanzhuHabitStatsVO {
    /**
     * 今日待打卡数
     */
    private Integer todayTotalCount;

    /**
     * 今日已打卡数
     */
    private Integer todayCheckedCount;

    /**
     * 当前周期习惯总数
     */
    private Integer currentPeriodHabitCount;

    /**
     * 当前周期达标习惯数
     */
    private Integer currentPeriodCompletedHabitCount;

    /**
     * 当前周期未达标习惯数
     */
    private Integer currentPeriodUncompletedHabitCount;

    /**
     * 当前周期整体完成率
     */
    private BigDecimal currentPeriodCompletionRate;

    /**
     * 最长连续打卡天数
     */
    private Integer maxContinuousDays;
}
