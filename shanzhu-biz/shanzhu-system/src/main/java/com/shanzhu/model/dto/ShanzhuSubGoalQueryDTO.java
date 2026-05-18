package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShanzhuSubGoalQueryDTO {
    /**
     * 所属目标ID
     */
    @NotBlank(message = "请选择目标")
    private String goalId;
}
