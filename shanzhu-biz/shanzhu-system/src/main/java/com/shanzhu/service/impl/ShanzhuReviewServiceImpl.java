package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanzhu.entity.ShanzhuCategory;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.entity.ShanzhuGoalProgress;
import com.shanzhu.entity.ShanzhuTag;
import com.shanzhu.entity.ShanzhuTagRelation;
import com.shanzhu.entity.ShanzhuTask;
import com.shanzhu.mapper.ShanzhuCategoryMapper;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.mapper.ShanzhuGoalProgressMapper;
import com.shanzhu.mapper.ShanzhuTagMapper;
import com.shanzhu.mapper.ShanzhuTagRelationMapper;
import com.shanzhu.mapper.ShanzhuTaskMapper;
import com.shanzhu.model.dto.ShanzhuReviewQueryDTO;
import com.shanzhu.model.vo.ShanzhuReviewVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuReviewService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShanzhuReviewServiceImpl implements ShanzhuReviewService {

    private static final String NORMAL_FLAG = "0";
    private static final String COMPLETED_STATUS = "completed";
    private static final String ABANDONED_STATUS = "abandoned";
    private static final String WEEK_REVIEW_TYPE = "week";
    private static final String MONTH_REVIEW_TYPE = "month";
    private static final String TASK_TAG_BIZ_TYPE = "task";
    private static final int LOW_PROGRESS_THRESHOLD = 30;
    private static final int LOW_COMPLETION_TASK_THRESHOLD = 1;

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Resource
    private ShanzhuTaskMapper shanzhuTaskMapper;

    @Resource
    private ShanzhuCategoryMapper shanzhuCategoryMapper;

    @Resource
    private ShanzhuGoalProgressMapper shanzhuGoalProgressMapper;

    @Resource
    private ShanzhuTagMapper shanzhuTagMapper;

    @Resource
    private ShanzhuTagRelationMapper shanzhuTagRelationMapper;

    @Override
    public ShanzhuReviewVO queryReview(ShanzhuReviewQueryDTO queryDTO) {
        ReviewPeriod reviewPeriod = resolveReviewPeriod(queryDTO);
        List<ShanzhuGoal> goals = queryCurrentUserGoals();
        List<ShanzhuTask> tasks = queryCurrentUserTasks();
        List<ShanzhuTask> completedTasksInPeriod = filterCompletedTasksInPeriod(tasks, reviewPeriod);
        List<ShanzhuGoalProgress> progressRecords = queryProgressRecords(reviewPeriod);
        Map<String, ShanzhuGoal> goalMap = goals.stream().collect(Collectors.toMap(ShanzhuGoal::getId, Function.identity()));
        Map<String, ShanzhuCategory> categoryMap = queryCategoryMap(goals);
        Map<String, List<ShanzhuTask>> goalIdToCompletedTasks = completedTasksInPeriod.stream()
                .filter(task -> StringUtils.hasText(task.getGoalId()))
                .collect(Collectors.groupingBy(ShanzhuTask::getGoalId));
        Map<String, List<ShanzhuGoalProgress>> goalIdToProgressRecords = progressRecords.stream()
                .filter(progress -> StringUtils.hasText(progress.getGoalId()))
                .collect(Collectors.groupingBy(ShanzhuGoalProgress::getGoalId));

        ShanzhuReviewVO reviewVO = new ShanzhuReviewVO();
        reviewVO.setReviewType(reviewPeriod.reviewType());
        reviewVO.setStartDate(reviewPeriod.startDate());
        reviewVO.setEndDate(reviewPeriod.endDate());
        fillOverview(reviewVO.getOverview(), goals, completedTasksInPeriod, progressRecords);
        reviewVO.setCategoryInvestmentStats(buildCategoryInvestmentStats(completedTasksInPeriod, goals, categoryMap, goalMap));
        reviewVO.setTagAnalysisStats(buildTagAnalysisStats(completedTasksInPeriod));
        reviewVO.setTaskEfficiencyTrends(buildTaskEfficiencyTrends(completedTasksInPeriod, reviewPeriod));
        reviewVO.setGoalAchievementStats(buildGoalAchievementStats(goals, goalIdToCompletedTasks, goalIdToProgressRecords, categoryMap));
        reviewVO.setAdjustmentSuggestions(buildAdjustmentSuggestions(reviewVO.getGoalAchievementStats()));
        reviewVO.setReviewSummary(buildReviewSummary(reviewVO));
        return reviewVO;
    }

    private ReviewPeriod resolveReviewPeriod(ShanzhuReviewQueryDTO queryDTO) {
        LocalDate today = LocalDate.now();
        String reviewType = queryDTO == null || !StringUtils.hasText(queryDTO.getReviewType())
                ? WEEK_REVIEW_TYPE
                : queryDTO.getReviewType();
        LocalDate startDate = queryDTO == null ? null : queryDTO.getStartDate();
        LocalDate endDate = queryDTO == null ? null : queryDTO.getEndDate();

        if (startDate != null && endDate != null) {
            return new ReviewPeriod(reviewType, startDate, endDate);
        }

        if (MONTH_REVIEW_TYPE.equals(reviewType)) {
            return new ReviewPeriod(reviewType, today.withDayOfMonth(1), today);
        }
        return new ReviewPeriod(WEEK_REVIEW_TYPE, today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), today);
    }

    private List<ShanzhuGoal> queryCurrentUserGoals() {
        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
        return shanzhuGoalMapper.selectList(queryWrapper);
    }

    private List<ShanzhuTask> queryCurrentUserTasks() {
        QueryWrapper<ShanzhuTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTask::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTask::getDelFlag, NORMAL_FLAG);
        return shanzhuTaskMapper.selectList(queryWrapper);
    }

    private List<ShanzhuTask> filterCompletedTasksInPeriod(List<ShanzhuTask> tasks, ReviewPeriod reviewPeriod) {
        return tasks.stream()
                .filter(task -> COMPLETED_STATUS.equals(task.getStatus()))
                .filter(task -> task.getCompletedTime() != null)
                .filter(task -> isDateInPeriod(task.getCompletedTime().toLocalDate(), reviewPeriod))
                .toList();
    }

    private List<ShanzhuGoalProgress> queryProgressRecords(ReviewPeriod reviewPeriod) {
        QueryWrapper<ShanzhuGoalProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoalProgress::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoalProgress::getDelFlag, NORMAL_FLAG)
                .ge(ShanzhuGoalProgress::getRecordDate, reviewPeriod.startDate())
                .le(ShanzhuGoalProgress::getRecordDate, reviewPeriod.endDate());
        return shanzhuGoalProgressMapper.selectList(queryWrapper);
    }

    private Map<String, ShanzhuCategory> queryCategoryMap(List<ShanzhuGoal> goals) {
        List<String> categoryIds = goals.stream()
                .map(ShanzhuGoal::getCategoryId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }

        QueryWrapper<ShanzhuCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(ShanzhuCategory::getId, categoryIds)
                .eq(ShanzhuCategory::getDelFlag, NORMAL_FLAG);
        return shanzhuCategoryMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(ShanzhuCategory::getId, Function.identity()));
    }

    private void fillOverview(ShanzhuReviewVO.ReviewOverview overview,
                              List<ShanzhuGoal> goals,
                              List<ShanzhuTask> completedTasks,
                              List<ShanzhuGoalProgress> progressRecords) {
        Set<String> progressedGoalIds = progressRecords.stream()
                .map(ShanzhuGoalProgress::getGoalId)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        long totalEstimatedMinutes = completedTasks.stream().mapToLong(task -> safeMinutes(task.getEstimatedMinutes())).sum();
        long totalActualMinutes = completedTasks.stream().mapToLong(task -> safeMinutes(task.getActualMinutes())).sum();

        overview.setCompletedTaskCount(completedTasks.size());
        overview.setProgressRecordCount(progressRecords.size());
        overview.setProgressedGoalCount(progressedGoalIds.size());
        overview.setCompletedGoalCount(goals.stream().filter(goal -> COMPLETED_STATUS.equals(goal.getStatus())).count());
        overview.setTotalEstimatedMinutes(totalEstimatedMinutes);
        overview.setTotalActualMinutes(totalActualMinutes);
        overview.setAverageEfficiencyRate(calculateRatio(totalActualMinutes, totalEstimatedMinutes));
    }

    private List<ShanzhuReviewVO.CategoryInvestmentStats> buildCategoryInvestmentStats(List<ShanzhuTask> completedTasks,
                                                                                       List<ShanzhuGoal> goals,
                                                                                       Map<String, ShanzhuCategory> categoryMap,
                                                                                       Map<String, ShanzhuGoal> goalMap) {
        long totalActualMinutes = completedTasks.stream().mapToLong(task -> safeMinutes(task.getActualMinutes())).sum();
        Map<String, List<ShanzhuTask>> categoryIdToTasks = completedTasks.stream()
                .collect(Collectors.groupingBy(task -> resolveTaskCategoryId(task, goalMap)));
        Map<String, List<ShanzhuGoal>> categoryIdToCompletedGoals = goals.stream()
                .filter(goal -> COMPLETED_STATUS.equals(goal.getStatus()))
                .collect(Collectors.groupingBy(goal -> defaultCategoryId(goal.getCategoryId())));

        return categoryIdToTasks.entrySet().stream()
                .map(entry -> {
                    String categoryId = entry.getKey();
                    List<ShanzhuTask> categoryTasks = entry.getValue();
                    ShanzhuCategory category = categoryMap.get(categoryId);
                    long actualMinutes = categoryTasks.stream().mapToLong(task -> safeMinutes(task.getActualMinutes())).sum();

                    ShanzhuReviewVO.CategoryInvestmentStats stats = new ShanzhuReviewVO.CategoryInvestmentStats();
                    stats.setCategoryId(categoryId);
                    stats.setCategoryName(category == null ? "未分类" : category.getName());
                    stats.setCategoryColor(category == null ? "" : category.getColor());
                    stats.setCompletedTaskCount(categoryTasks.size());
                    stats.setActualMinutes(actualMinutes);
                    stats.setInvestmentRatio(calculateRatio(actualMinutes, totalActualMinutes));
                    stats.setCompletedGoalCount(categoryIdToCompletedGoals.getOrDefault(categoryId, List.of()).size());
                    return stats;
                })
                .sorted(Comparator.comparing(ShanzhuReviewVO.CategoryInvestmentStats::getActualMinutes).reversed())
                .toList();
    }

    private List<ShanzhuReviewVO.TagAnalysisStats> buildTagAnalysisStats(List<ShanzhuTask> completedTasks) {
        List<String> completedTaskIds = completedTasks.stream().map(ShanzhuTask::getId).toList();
        if (completedTaskIds.isEmpty()) {
            return List.of();
        }

        QueryWrapper<ShanzhuTagRelation> relationQueryWrapper = new QueryWrapper<>();
        relationQueryWrapper.lambda()
                .eq(ShanzhuTagRelation::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTagRelation::getBizType, TASK_TAG_BIZ_TYPE)
                .eq(ShanzhuTagRelation::getDelFlag, NORMAL_FLAG)
                .in(ShanzhuTagRelation::getBizId, completedTaskIds);
        List<ShanzhuTagRelation> relations = shanzhuTagRelationMapper.selectList(relationQueryWrapper);
        if (relations.isEmpty()) {
            return List.of();
        }

        Map<String, ShanzhuTask> completedTaskMap = completedTasks.stream()
                .collect(Collectors.toMap(ShanzhuTask::getId, Function.identity()));
        Map<String, ShanzhuTag> tagMap = queryTagMap(relations);
        Map<String, List<ShanzhuTagRelation>> tagIdToRelations = relations.stream()
                .collect(Collectors.groupingBy(ShanzhuTagRelation::getTagId));

        return tagIdToRelations.entrySet().stream()
                .map(entry -> buildTagAnalysisStats(entry.getKey(), entry.getValue(), completedTaskMap, tagMap))
                .sorted(Comparator.comparing(ShanzhuReviewVO.TagAnalysisStats::getActualMinutes).reversed())
                .toList();
    }

    private Map<String, ShanzhuTag> queryTagMap(List<ShanzhuTagRelation> relations) {
        List<String> tagIds = relations.stream()
                .map(ShanzhuTagRelation::getTagId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (tagIds.isEmpty()) {
            return Map.of();
        }

        QueryWrapper<ShanzhuTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(ShanzhuTag::getId, tagIds)
                .eq(ShanzhuTag::getDelFlag, NORMAL_FLAG);
        return shanzhuTagMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(ShanzhuTag::getId, Function.identity()));
    }

    private ShanzhuReviewVO.TagAnalysisStats buildTagAnalysisStats(String tagId,
                                                                   List<ShanzhuTagRelation> relations,
                                                                   Map<String, ShanzhuTask> completedTaskMap,
                                                                   Map<String, ShanzhuTag> tagMap) {
        ShanzhuTag tag = tagMap.get(tagId);
        List<ShanzhuTask> tagTasks = relations.stream()
                .map(relation -> completedTaskMap.get(relation.getBizId()))
                .filter(task -> task != null)
                .toList();
        long actualMinutes = tagTasks.stream().mapToLong(task -> safeMinutes(task.getActualMinutes())).sum();

        ShanzhuReviewVO.TagAnalysisStats stats = new ShanzhuReviewVO.TagAnalysisStats();
        stats.setTagId(tagId);
        stats.setTagName(tag == null ? "未知标签" : tag.getName());
        stats.setTagColor(tag == null ? "" : tag.getColor());
        stats.setTagType(tag == null ? "" : tag.getTagType());
        stats.setTaskCount(tagTasks.size());
        stats.setCompletedTaskCount(tagTasks.size());
        stats.setCompletionRate(tagTasks.isEmpty() ? 0D : 1D);
        stats.setActualMinutes(actualMinutes);
        return stats;
    }

    private List<ShanzhuReviewVO.TaskEfficiencyTrend> buildTaskEfficiencyTrends(List<ShanzhuTask> completedTasks,
                                                                                ReviewPeriod reviewPeriod) {
        Map<LocalDate, List<ShanzhuTask>> dateToTasks = completedTasks.stream()
                .collect(Collectors.groupingBy(task -> task.getCompletedTime().toLocalDate()));

        return reviewPeriod.startDate().datesUntil(reviewPeriod.endDate().plusDays(1))
                .map(date -> buildDailyTaskEfficiencyTrend(date, dateToTasks.getOrDefault(date, List.of())))
                .toList();
    }

    private ShanzhuReviewVO.TaskEfficiencyTrend buildDailyTaskEfficiencyTrend(LocalDate date, List<ShanzhuTask> tasks) {
        long estimatedMinutes = tasks.stream().mapToLong(task -> safeMinutes(task.getEstimatedMinutes())).sum();
        long actualMinutes = tasks.stream().mapToLong(task -> safeMinutes(task.getActualMinutes())).sum();

        ShanzhuReviewVO.TaskEfficiencyTrend trend = new ShanzhuReviewVO.TaskEfficiencyTrend();
        trend.setDate(date);
        trend.setCompletedTaskCount(tasks.size());
        trend.setEstimatedMinutes(estimatedMinutes);
        trend.setActualMinutes(actualMinutes);
        trend.setEfficiencyRate(calculateRatio(actualMinutes, estimatedMinutes));
        return trend;
    }

    private List<ShanzhuReviewVO.GoalAchievementStats> buildGoalAchievementStats(List<ShanzhuGoal> goals,
                                                                                 Map<String, List<ShanzhuTask>> goalIdToCompletedTasks,
                                                                                 Map<String, List<ShanzhuGoalProgress>> goalIdToProgressRecords,
                                                                                 Map<String, ShanzhuCategory> categoryMap) {
        return goals.stream()
                .filter(this::isActiveGoal)
                .map(goal -> buildGoalAchievementStats(goal, goalIdToCompletedTasks, goalIdToProgressRecords, categoryMap))
                .sorted(Comparator.comparing(ShanzhuReviewVO.GoalAchievementStats::isNeedAdjustment).reversed()
                        .thenComparing(ShanzhuReviewVO.GoalAchievementStats::getProgress, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private ShanzhuReviewVO.GoalAchievementStats buildGoalAchievementStats(ShanzhuGoal goal,
                                                                           Map<String, List<ShanzhuTask>> goalIdToCompletedTasks,
                                                                           Map<String, List<ShanzhuGoalProgress>> goalIdToProgressRecords,
                                                                           Map<String, ShanzhuCategory> categoryMap) {
        List<ShanzhuTask> completedTasks = goalIdToCompletedTasks.getOrDefault(goal.getId(), List.of());
        List<ShanzhuGoalProgress> progressRecords = goalIdToProgressRecords.getOrDefault(goal.getId(), List.of());
        ShanzhuCategory category = categoryMap.get(goal.getCategoryId());
        String adjustmentReason = resolveAdjustmentReason(goal, completedTasks, progressRecords);

        ShanzhuReviewVO.GoalAchievementStats stats = new ShanzhuReviewVO.GoalAchievementStats();
        stats.setGoalId(goal.getId());
        stats.setGoalTitle(goal.getTitle());
        stats.setCategoryName(category == null ? "未分类" : category.getName());
        stats.setCategoryColor(category == null ? "" : category.getColor());
        stats.setStatus(goal.getStatus());
        stats.setProgress(goal.getProgress());
        stats.setProgressRecordCount(progressRecords.size());
        stats.setCompletedTaskCount(completedTasks.size());
        stats.setNeedAdjustment(StringUtils.hasText(adjustmentReason));
        stats.setAdjustmentReason(adjustmentReason);
        return stats;
    }

    private List<ShanzhuReviewVO.AdjustmentSuggestion> buildAdjustmentSuggestions(List<ShanzhuReviewVO.GoalAchievementStats> goalStats) {
        return goalStats.stream()
                .filter(ShanzhuReviewVO.GoalAchievementStats::isNeedAdjustment)
                .map(goalStat -> {
                    ShanzhuReviewVO.AdjustmentSuggestion suggestion = new ShanzhuReviewVO.AdjustmentSuggestion();
                    suggestion.setGoalId(goalStat.getGoalId());
                    suggestion.setGoalTitle(goalStat.getGoalTitle());
                    suggestion.setSuggestionType("goal_adjustment");
                    suggestion.setSuggestionContent(goalStat.getAdjustmentReason());
                    return suggestion;
                })
                .toList();
    }

    private List<String> buildReviewSummary(ShanzhuReviewVO reviewVO) {
        ShanzhuReviewVO.ReviewOverview overview = reviewVO.getOverview();
        return List.of(
                String.format("本次复盘周期内完成了 %s 个任务，推进了 %s 个目标。", overview.getCompletedTaskCount(), overview.getProgressedGoalCount()),
                String.format("累计实际投入 %s 分钟，预计投入 %s 分钟，效率比为 %.2f。", overview.getTotalActualMinutes(), overview.getTotalEstimatedMinutes(), overview.getAverageEfficiencyRate()),
                String.format("当前识别到 %s 个目标需要关注或调整。", reviewVO.getAdjustmentSuggestions().size())
        );
    }

    private String resolveTaskCategoryId(ShanzhuTask task, Map<String, ShanzhuGoal> goalMap) {
        ShanzhuGoal goal = goalMap.get(task.getGoalId());
        return goal == null ? "" : defaultCategoryId(goal.getCategoryId());
    }

    private String defaultCategoryId(String categoryId) {
        return StringUtils.hasText(categoryId) ? categoryId : "";
    }

    private String resolveAdjustmentReason(ShanzhuGoal goal,
                                           List<ShanzhuTask> completedTasks,
                                           List<ShanzhuGoalProgress> progressRecords) {
        int progress = goal.getProgress() == null ? 0 : goal.getProgress();
        if (progress < LOW_PROGRESS_THRESHOLD && completedTasks.size() <= LOW_COMPLETION_TASK_THRESHOLD && progressRecords.isEmpty()) {
            return "目标进度偏低，且本周期任务和进展较少，建议拆解下一步行动。";
        }
        if (goal.getDeadline() != null && goal.getDeadline().isBefore(LocalDate.now()) && !COMPLETED_STATUS.equals(goal.getStatus())) {
            return "目标已超过截止日期但未完成，建议调整截止日期或重新评估目标范围。";
        }
        if (progressRecords.isEmpty() && !COMPLETED_STATUS.equals(goal.getStatus())) {
            return "本周期没有新增进展记录，建议确认目标是否仍需持续推进。";
        }
        return "";
    }

    private boolean isActiveGoal(ShanzhuGoal goal) {
        return !COMPLETED_STATUS.equals(goal.getStatus()) && !ABANDONED_STATUS.equals(goal.getStatus());
    }

    private boolean isDateInPeriod(LocalDate date, ReviewPeriod reviewPeriod) {
        return !date.isBefore(reviewPeriod.startDate()) && !date.isAfter(reviewPeriod.endDate());
    }

    private long safeMinutes(Integer minutes) {
        return minutes == null ? 0L : minutes;
    }

    private double calculateRatio(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0D;
        }
        return Math.round((double) numerator * 100D / (double) denominator) / 100D;
    }

    private record ReviewPeriod(String reviewType, LocalDate startDate, LocalDate endDate) {
    }
}
