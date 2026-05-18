package com.shanzhu.model.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ShanzhuHabitCheckinQueryDTO {
    /**
     * 习惯ID
     */
    private String habitId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;
}
