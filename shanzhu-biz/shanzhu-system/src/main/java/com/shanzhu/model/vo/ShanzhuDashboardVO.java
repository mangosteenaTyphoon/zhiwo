package com.shanzhu.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShanzhuDashboardVO {
    /**
     * 概览统计
     */
    private OverviewStats overview = new OverviewStats();

    /**
     * 分类维度统计
     */
    private List<CategoryStats> categoryStats = new ArrayList<>();

    /**
     * 时间维度统计
     */
    private TimeStats timeStats = new TimeStats();

    /**
     * 风险统计
     */
    private RiskStats riskStats = new RiskStats();

    @Data
    public static class OverviewStats {
        /**
         * 目标总数
         */
        private long totalGoalCount;

        /**
         * 进行中目标数量
         */
        private long inProgressGoalCount;

        /**
         * 已完成目标数量
         */
        private long completedGoalCount;

        /**
         * 暂停目标数量
         */
        private long pausedGoalCount;

        /**
         * 放弃目标数量
         */
        private long abandonedGoalCount;

        /**
         * 任务总数
         */
        private long totalTaskCount;

        /**
         * 待处理任务数量
         */
        private long pendingTaskCount;

        /**
         * 已完成任务数量
         */
        private long completedTaskCount;

        /**
         * 逾期目标数量
         */
        private long overdueGoalCount;

        /**
         * 逾期任务数量
         */
        private long overdueTaskCount;

        /**
         * 长时间未推进目标数量
         */
        private long longTimeNoProgressGoalCount;

        /**
         * 高优先级未完成任务数量
         */
        private long highPriorityUnfinishedTaskCount;
    }

    @Data
    public static class CategoryStats {
        /**
         * 分类ID
         */
        private String categoryId;

        /**
         * 分类名称
         */
        private String categoryName;

        /**
         * 分类颜色
         */
        private String categoryColor;

        /**
         * 目标数量
         */
        private long goalCount;

        /**
         * 已完成目标数量
         */
        private long completedGoalCount;

        /**
         * 任务数量
         */
        private long taskCount;

        /**
         * 已完成任务数量
         */
        private long completedTaskCount;
    }

    @Data
    public static class TimeStats {
        /**
         * 今日完成任务数
         */
        private long todayCompletedTaskCount;

        /**
         * 本周完成任务数
         */
        private long weekCompletedTaskCount;

        /**
         * 本月完成任务数
         */
        private long monthCompletedTaskCount;

        /**
         * 最近7天任务完成趋势
         */
        private List<DailyTaskCompletionStats> recentSevenDayTaskCompletionTrend = new ArrayList<>();
    }

    @Data
    public static class DailyTaskCompletionStats {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 完成任务数
         */
        private long completedTaskCount;
    }

    @Data
    public static class RiskStats {
        /**
         * 逾期目标
         */
        private List<RiskGoal> overdueGoals = new ArrayList<>();

        /**
         * 逾期任务
         */
        private List<RiskTask> overdueTasks = new ArrayList<>();

        /**
         * 长时间未推进目标
         */
        private List<RiskGoal> longTimeNoProgressGoals = new ArrayList<>();

        /**
         * 高优先级未完成任务
         */
        private List<RiskTask> highPriorityUnfinishedTasks = new ArrayList<>();
    }

    @Data
    public static class RiskGoal {
        /**
         * 目标ID
         */
        private String id;

        /**
         * 目标名称
         */
        private String title;

        /**
         * 分类ID
         */
        private String categoryId;

        /**
         * 分类名称
         */
        private String categoryName;

        /**
         * 分类颜色
         */
        private String categoryColor;

        /**
         * 目标状态
         */
        private String status;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 当前进度
         */
        private Integer progress;

        /**
         * 截止日期
         */
        private LocalDate deadline;

        /**
         * 逾期天数
         */
        private long overdueDays;

        /**
         * 最近推进日期
         */
        private LocalDate lastProgressDate;

        /**
         * 距最近推进天数
         */
        private long daysSinceLastProgress;
    }

    @Data
    public static class RiskTask {
        /**
         * 任务ID
         */
        private String id;

        /**
         * 任务标题
         */
        private String title;

        /**
         * 所属目标ID
         */
        private String goalId;

        /**
         * 所属目标名称
         */
        private String goalTitle;

        /**
         * 分类ID
         */
        private String categoryId;

        /**
         * 分类名称
         */
        private String categoryName;

        /**
         * 分类颜色
         */
        private String categoryColor;

        /**
         * 任务状态
         */
        private String status;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 截止日期
         */
        private LocalDate deadline;

        /**
         * 逾期天数
         */
        private long overdueDays;
    }
}
