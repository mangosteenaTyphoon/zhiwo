package com.shanzhu.model.dto;

import com.shanzhu.mybatis.model.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ShanzhuGoalQueryDTO extends BaseDTO {
    /**
     * 关键词
     */
    private String keyword;

    /**
     * 分类ID
     */
    private String categoryId;

    /**
     * 目标状态
     */
    private String status;

    /**
     * 标签ID列表
     */
    private List<String> tagIds;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 截止日期
     */
    private LocalDate deadline;
}
