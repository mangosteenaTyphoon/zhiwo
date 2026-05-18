package com.shanzhu.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ShanzhuHabitCheckinVO {
    /**
     * 打卡记录ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 习惯ID
     */
    private String habitId;

    /**
     * 习惯名称
     */
    private String habitTitle;

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
     * 打卡日期
     */
    private LocalDate checkinDate;

    /**
     * 实际完成值
     */
    private BigDecimal actualValue;

    /**
     * 单位
     */
    private String unit;

    /**
     * 打卡备注
     */
    private String note;

    /**
     * 状态感受
     */
    private String mood;

    /**
     * 打卡状态
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
