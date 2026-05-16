package com.shanzhu.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ShanzhuGoalVO {
    /**
     * 目标ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 分类颜色
     */
    private String categoryColor;

    /**
     * 目标名称
     */
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
     * 完成时间
     */
    private LocalDateTime completedTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 标签列表
     */
    private List<ShanzhuTagVO> tags;
}
