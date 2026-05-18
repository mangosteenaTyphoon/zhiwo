package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShanzhuTaskSortDTO {
    /**
     * 任务ID
     */
    @NotBlank(message = "请选择任务")
    private String id;

    /**
     * 排序
     */
    @NotNull(message = "请输入排序")
    private Integer sortOrder;
}
