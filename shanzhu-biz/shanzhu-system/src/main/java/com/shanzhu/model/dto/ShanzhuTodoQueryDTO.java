package com.shanzhu.model.dto;

import com.shanzhu.common.model.dto.BasePageDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class ShanzhuTodoQueryDTO extends BasePageDTO {
    /**
     * 关键词
     */
    private String keyword;

    /**
     * 状态：inbox 收集箱, today 今日待办, done 已完成, archived 已归档, converted 已转任务
     */
    private String status;

    /**
     * 关联目标ID
     */
    private String goalId;

    /**
     * 关联子目标ID
     */
    private String subGoalId;

    /**
     * 计划处理日期
     */
    private LocalDate plannedDate;

    /**
     * 优先级
     */
    private Integer priority;
}
