package com.shanzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shanzhu.mybatis.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("shanzhu_sub_goal")
public class ShanzhuSubGoal extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 所属目标ID
     */
    private String goalId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 子目标名称
     */
    private String title;

    /**
     * 子目标描述
     */
    private String description;

    /**
     * 子目标状态
     */
    private String status;

    /**
     * 子目标进度，0-100
     */
    private Integer progress;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;
}
