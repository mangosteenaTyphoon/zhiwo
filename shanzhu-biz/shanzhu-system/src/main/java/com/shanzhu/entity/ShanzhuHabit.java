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
@TableName("shanzhu_habit")
public class ShanzhuHabit extends BaseEntity {
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
     * 关联目标ID，可为空
     */
    private String goalId;

    /**
     * 关联子目标ID，可为空
     */
    private String subGoalId;

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
}
