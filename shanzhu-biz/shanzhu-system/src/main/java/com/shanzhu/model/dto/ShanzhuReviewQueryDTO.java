package com.shanzhu.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ShanzhuReviewQueryDTO {
    /**
     * 复盘类型：week 周复盘，month 月复盘
     */
    private String reviewType;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;
}
