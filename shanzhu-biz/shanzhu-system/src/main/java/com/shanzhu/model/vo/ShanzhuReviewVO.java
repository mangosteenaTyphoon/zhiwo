package com.shanzhu.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShanzhuReviewVO {
    /**
     * 复盘类型：week 周复盘，month 月复盘
     */
    private String reviewType;

    /**
     * 复盘开始日期
     */
    private LocalDate startDate;

    /**
     * 复盘结束日期
     */
    private LocalDate endDate;

    /**
     * 复盘概览
     */
    private ReviewOverview overview = new ReviewOverview();

    /**
     * 分类投入分析
     */
    private List<CategoryInvestmentStats> categoryInvestmentStats = new ArrayList<>();

    /**
     * 标签维度分析
     */
    private List<TagAnalysisStats> tagAnalysisStats = new ArrayList<>();

    /**
     * 任务效率趋势
     */
    private List<TaskEfficiencyTrend> taskEfficiencyTrends = new ArrayList<>();

    /**
     * 目标达成分析
     */
    private List<GoalAchievementStats> goalAchievementStats = new ArrayList<>();

    /**
     * 目标调整建议
     */
    private List<AdjustmentSuggestion> adjustmentSuggestions = new ArrayList<>();

    /**
     * 复盘摘要
     */
    private List<String> reviewSummary = new ArrayList<>();

    @Data
    public static class ReviewOverview {
        /**
         * 完成任务数
         */
        private long completedTaskCount;

        /**
         * 新增进展记录数
         */
        private long progressRecordCount;

        /**
         * 推进目标数
         */
        private long progressedGoalCount;

        /**
         * 完成目标数
         */
        private long completedGoalCount;

        /**
         * 总预计耗时，单位分钟
         */
        private long totalEstimatedMinutes;

        /**
         * 总实际耗时，单位分钟
         */
        private long totalActualMinutes;

        /**
         * 平均任务完成效率，实际耗时 / 预计耗时
         */
        private double averageEfficiencyRate;

        /**
         * 新增 Todo 数
         */
        private long newTodoCount;

        /**
         * 完成 Todo 数
         */
        private long completedTodoCount;

        /**
         * 转任务 Todo 数
         */
        private long convertedTodoCount;

        /**
         * 长期未处理 Todo 数（超过7天）
         */
        private long longPendingTodoCount;
    }

    @Data
    public static class CategoryInvestmentStats {
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
         * 完成任务数
         */
        private long completedTaskCount;

        /**
         * 实际投入分钟数
         */
        private long actualMinutes;

        /**
         * 投入占比
         */
        private double investmentRatio;

        /**
         * 完成目标数
         */
        private long completedGoalCount;
    }

    @Data
    public static class TagAnalysisStats {
        /**
         * 标签ID
         */
        private String tagId;

        /**
         * 标签名称
         */
        private String tagName;

        /**
         * 标签颜色
         */
        private String tagColor;

        /**
         * 标签类型
         */
        private String tagType;

        /**
         * 关联任务数
         */
        private long taskCount;

        /**
         * 已完成任务数
         */
        private long completedTaskCount;

        /**
         * 完成率
         */
        private double completionRate;

        /**
         * 实际投入分钟数
         */
        private long actualMinutes;
    }

    @Data
    public static class TaskEfficiencyTrend {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 完成任务数
         */
        private long completedTaskCount;

        /**
         * 预计耗时，单位分钟
         */
        private long estimatedMinutes;

        /**
         * 实际耗时，单位分钟
         */
        private long actualMinutes;

        /**
         * 效率比，实际耗时 / 预计耗时
         */
        private double efficiencyRate;
    }

    @Data
    public static class GoalAchievementStats {
        /**
         * 目标ID
         */
        private String goalId;

        /**
         * 目标名称
         */
        private String goalTitle;

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
         * 当前进度
         */
        private Integer progress;

        /**
         * 复盘期内进展记录数量
         */
        private long progressRecordCount;

        /**
         * 复盘期内完成任务数量
         */
        private long completedTaskCount;

        /**
         * 是否建议调整
         */
        private boolean needAdjustment;

        /**
         * 调整原因
         */
        private String adjustmentReason;
    }

    @Data
    public static class AdjustmentSuggestion {
        /**
         * 目标ID
         */
        private String goalId;

        /**
         * 目标名称
         */
        private String goalTitle;

        /**
         * 建议类型
         */
        private String suggestionType;

        /**
         * 建议内容
         */
        private String suggestionContent;
    }

    @Data
    public static class TodoStats {
        /**
         * Todo 状态
         */
        private String status;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 数量
         */
        private long count;

        /**
         * 占比
         */
        private double ratio;
    }
}
