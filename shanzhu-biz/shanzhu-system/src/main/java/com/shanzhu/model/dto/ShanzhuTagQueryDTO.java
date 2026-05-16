package com.shanzhu.model.dto;

import lombok.Data;

@Data
public class ShanzhuTagQueryDTO {
    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 标签类型
     */
    private String tagType;
}
