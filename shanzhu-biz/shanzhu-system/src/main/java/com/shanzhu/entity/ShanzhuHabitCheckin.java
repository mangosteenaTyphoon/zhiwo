package com.shanzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shanzhu.mybatis.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("shanzhu_habit_checkin")
public class ShanzhuHabitCheckin extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
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
     * 打卡时关联目标ID，可为空
     */
    private String goalId;

    /**
     * 打卡时关联子目标ID，可为空
     */
    private String subGoalId;

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
}
