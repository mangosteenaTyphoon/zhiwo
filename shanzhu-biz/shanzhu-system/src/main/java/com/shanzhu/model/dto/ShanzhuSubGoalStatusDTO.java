package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShanzhuSubGoalStatusDTO {
    /**
     * 子目标ID
     */
    @NotBlank(message = "请选择子目标")
    private String id;

    /**
     * 子目标状态
     */
    @NotBlank(message = "请选择子目标状态")
    private String status;
}
