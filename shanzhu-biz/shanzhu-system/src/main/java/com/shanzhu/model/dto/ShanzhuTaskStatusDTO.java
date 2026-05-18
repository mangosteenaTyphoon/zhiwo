package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShanzhuTaskStatusDTO {
    /**
     * 任务ID
     */
    @NotBlank(message = "请选择任务")
    private String id;

    /**
     * 任务状态
     */
    @NotBlank(message = "请选择任务状态")
    private String status;
}
