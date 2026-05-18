package com.shanzhu.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ShanzhuGoalProgressVO {
    /**
     * 主键
     */
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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
