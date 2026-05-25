package com.shanzhu.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class ShanzhuTodayWorkVO {
    /**
     * 今日概览统计
     */
    private TodayOverview overview;

    /**
     * 今日任务列表
     */
    private List<ShanzhuTaskVO> tasks;

    /**
     * 今日习惯列表
     */
    private List<ShanzhuHabitTodayVO> habits;

    /**
     * 今日Todo列表
     */
    private List<ShanzhuTodoVO> todos;

    @Data
    public static class TodayOverview {
        /**
         * 今日任务总数
         */
        private Integer taskTotalCount;

        /**
         * 今日已完成任务数
         */
        private Integer taskCompletedCount;

        /**
         * 今日习惯总数
         */
        private Integer habitTotalCount;

        /**
         * 今日已打卡习惯数
         */
        private Integer habitCheckedCount;

        /**
         * 今日Todo总数
         */
        private Integer todoTotalCount;

        /**
         * 今日已完成Todo数
         */
        private Integer todoCompletedCount;
    }
}
