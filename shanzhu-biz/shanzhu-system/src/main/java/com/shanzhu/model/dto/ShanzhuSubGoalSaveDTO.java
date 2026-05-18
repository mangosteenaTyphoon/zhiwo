package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ShanzhuSubGoalSaveDTO {
    /**
     * 子目标ID，有值时更新子目标
     */
    private String id;

    /**
     * 所属目标ID
     */
    @NotBlank(message = "请选择目标")
    private String goalId;

    /**
     * 子目标名称
     */
    @NotBlank(message = "请输入子目标名称")
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
}
