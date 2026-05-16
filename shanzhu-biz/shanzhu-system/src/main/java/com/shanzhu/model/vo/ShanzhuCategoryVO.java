package com.shanzhu.model.vo;

import lombok.Data;

@Data
public class ShanzhuCategoryVO {
    /**
     * 分类ID
     */
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 分类编码
     */
    private String code;

    /**
     * 图标
     */
    private String icon;

    /**
     * 颜色
     */
    private String color;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 是否内置
     */
    private String builtIn;

    /**
     * 是否启用
     */
    private String enabled;
}
