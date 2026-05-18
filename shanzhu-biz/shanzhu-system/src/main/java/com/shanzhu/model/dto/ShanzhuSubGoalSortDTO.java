package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShanzhuSubGoalSortDTO {
    /**
     * 子目标ID
     */
    @NotBlank(message = "请选择子目标")
    private String id;

    /**
     * 排序
     */
    @NotNull(message = "请输入排序值")
    private Integer sortOrder;
}
