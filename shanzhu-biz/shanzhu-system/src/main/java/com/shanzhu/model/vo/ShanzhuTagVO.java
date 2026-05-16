package com.shanzhu.model.vo;

import lombok.Data;

@Data
public class ShanzhuTagVO {
    /**
     * 标签ID
     */
    private String id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签颜色
     */
    private String color;

    /**
     * 标签类型
     */
    private String tagType;

    /**
     * 使用次数
     */
    private Integer usageCount;

    /**
     * 是否内置
     */
    private String builtIn;
}
