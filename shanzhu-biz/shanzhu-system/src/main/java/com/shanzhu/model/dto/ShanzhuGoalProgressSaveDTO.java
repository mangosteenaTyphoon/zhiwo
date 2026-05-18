package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShanzhuGoalProgressSaveDTO {
    /**
     * 目标ID
     */
    @NotBlank(message = "请选择目标")
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
     * 进展标题
     */
    @NotBlank(message = "请输入进展标题")
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
