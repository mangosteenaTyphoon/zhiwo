package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ShanzhuHabitCheckinSaveDTO {
    /**
     * 习惯ID
     */
    @NotBlank(message = "请选择习惯")
    private String habitId;

    /**
     * 打卡日期，不传默认为今天
     */
    private LocalDate checkinDate;

    /**
     * 实际完成值
     */
    private BigDecimal actualValue;

    /**
     * 打卡备注
     */
    private String note;

    /**
     * 状态感受
     */
    private String mood;
}
