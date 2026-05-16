package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ShanzhuGoalSaveDTO {
    /**
     * 目标ID，有值时更新目标
     */
    private String id;

    /**
     * 分类ID
     */
    @NotBlank(message = "请选择目标分类")
    private String categoryId;

    /**
     * 目标名称
     */
    @NotBlank(message = "请输入目标名称")
    private String title;

    /**
     * 目标描述
     */
    private String description;

    /**
     * 目标类型
     */
    private String goalType;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 目标状态
     */
    private String status;

    /**
     * 当前进度，0-100
     */
    private Integer progress;

    /**
     * 进度模式
     */
    private String progressMode;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 备注
     */
    private String remark;

    /**
     * 标签ID列表
     */
    private List<String> tagIds;
}
