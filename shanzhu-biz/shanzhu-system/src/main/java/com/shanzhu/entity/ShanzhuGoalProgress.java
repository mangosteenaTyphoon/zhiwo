package com.shanzhu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shanzhu.mybatis.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("shanzhu_goal_progress")
public class ShanzhuGoalProgress extends BaseEntity {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 目标ID
     */
    private String goalId;

    /**
     * 子目标ID，可为空
     */
    private String subGoalId;

    /**
     * 任务ID，可为空
     */
    private String taskId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 进展标题
     */
    private String title;

    /**
     * 进展内容
     */
    private String content;

    /**
     * 变更前进度
     */
    private Integer progressBefore;

    /**
     * 变更后进度
     */
    private Integer progressAfter;

    /**
     * 记录日期
     */
    private LocalDate recordDate;
}
