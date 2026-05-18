package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShanzhuSubGoalProgressDTO {
    /**
     * 子目标ID
     */
    @NotBlank(message = "请选择子目标")
    private String id;

    /**
     * 子目标进度，0-100
     */
    @NotNull(message = "请输入子目标进度")
    private Integer progress;
}
