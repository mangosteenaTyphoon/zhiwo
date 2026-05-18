package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ShanzhuHabitSaveDTO {
    /**
     * 习惯ID，有值时更新习惯
     */
    private String id;

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
    @NotBlank(message = "请输入习惯名称")
    private String title;

    /**
     * 习惯描述
     */
    private String description;

    /**
     * 频率类型
     */
    @NotBlank(message = "请选择频率类型")
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
