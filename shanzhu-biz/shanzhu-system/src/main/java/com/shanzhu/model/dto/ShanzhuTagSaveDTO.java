package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShanzhuTagSaveDTO {
    /**
     * 标签名称
     */
    @NotBlank(message = "请输入标签名称")
    private String name;

    /**
     * 标签颜色
     */
    private String color;

    /**
     * 标签类型
     */
    private String tagType;
}
