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
@TableName("shanzhu_todo")
public class ShanzhuTodo extends BaseEntity {
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
     * Todo标题
     */
    private String title;

    /**
     * Todo说明
     */
    private String description;

    /**
     * 状态：inbox 收集箱, today 今日待办, done 已完成, archived 已归档, converted 已转任务
     */
    private String status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 计划处理日期
     */
    private LocalDate plannedDate;

    /**
     * 截止时间
     */
    private LocalDateTime deadline;

    /**
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 预计耗时，单位分钟
     */
    private Integer estimatedMinutes;

    /**
     * 实际耗时，单位分钟
     */
    private Integer actualMinutes;

    /**
     * 关联任务ID（转为任务后填充）
     */
    private String taskId;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 备注
     */
    private String remark;
}
