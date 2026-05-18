package com.shanzhu.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShanzhuHabitVO {
    /**
     * 习惯ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 关联目标ID，可为空
     */
    private String goalId;

    /**
     * 目标名称
     */
    private String goalTitle;

    /**
     * 关联子目标ID，可为空
     */
    private String subGoalId;

    /**
     * 子目标名称
     */
    private String subGoalTitle;

    /**
     * 习惯名称
     */
    private String title;

    /**
     * 习惯描述
     */
    private String description;

    /**
     * 频率类型
     */
    private String frequencyType;

    /**
     * 每周期目标次数
     */
    private Integer targetCount;

    /**
     * 每次目标值
     */
    private BigDecimal targetValue;

    /**
     * 目标单位
     */
    private String unit;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 习惯状态
     */
    private String status;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;

    /**
     * 今日是否已打卡
     */
    private Boolean todayChecked;

    /**
     * 今日打卡记录ID
     */
    private String todayCheckinId;

    /**
     * 当前周期已打卡次数
     */
    private Integer currentPeriodCheckedCount;

    /**
     * 当前周期目标次数
     */
    private Integer currentPeriodTargetCount;

    /**
     * 当前周期完成率
     */
    private BigDecimal currentPeriodCompletionRate;

    /**
     * 连续打卡天数
     */
    private Integer continuousDays;

    /**
     * 最近打卡记录
     */
    private List<ShanzhuHabitCheckinVO> recentCheckins;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
