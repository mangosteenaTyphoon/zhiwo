package com.shanzhu.model.dto;

import lombok.Data;

@Data
public class ShanzhuCategorySaveDTO {
    /**
     * 分类名称
     */
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;
}
