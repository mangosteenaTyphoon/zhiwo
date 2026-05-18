package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanzhu.entity.ShanzhuCategory;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.entity.ShanzhuGoalProgress;
import com.shanzhu.entity.ShanzhuTask;
import com.shanzhu.mapper.ShanzhuCategoryMapper;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.mapper.ShanzhuGoalProgressMapper;
import com.shanzhu.mapper.ShanzhuTaskMapper;
import com.shanzhu.model.vo.ShanzhuDashboardVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuDashboardService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ShanzhuDashboardServiceImpl implements ShanzhuDashboardService {

    private static final String NORMAL_FLAG = "0";
    private static final String COMPLETED_STATUS = "completed";
    private static final String IN_PROGRESS_STATUS = "in_progress";
    private static final String PAUSED_STATUS = "paused";
    private static final String ABANDONED_STATUS = "abandoned";
    private static final int HIGH_PRIORITY = 1;
    private static final int LONG_TIME_NO_PROGRESS_DAYS = 7;
    private static final int RISK_LIST_LIMIT = 10;

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Resource
    private ShanzhuTaskMapper shanzhuTaskMapper;

    @Resource
    private ShanzhuCategoryMapper shanzhuCategoryMapper;

    @Resource
    private ShanzhuGoalProgressMapper shanzhuGoalProgressMapper;

    @Override
    public ShanzhuDashboardVO queryDashboard() {
        List<ShanzhuGoal> goals = queryCurrentUserGoals();
        List<ShanzhuTask> tasks = queryCurrentUserTasks();
        Map<String, ShanzhuCategory> categoryMap = queryCategoryMap(goals);
        Map<String, ShanzhuGoal> goalMap = goals.stream().collect(Collectors.toMap(ShanzhuGoal::getId, Function.identity()));
        Map<String, LocalDate> goalLastProgressDateMap = queryGoalLastProgressDateMap();

        ShanzhuDashboardVO dashboardVO = new ShanzhuDashboardVO();
        fillOverview(dashboardVO.getOverview(), goals, tasks, goalLastProgressDateMap);
        fillCategoryStats(dashboardVO, goals, tasks, categoryMap, goalMap);
        fillTimeStats(dashboardVO.getTimeStats(), tasks);
        fillRiskStats(dashboardVO.getRiskStats(), goals, tasks, categoryMap, goalMap, goalLastProgressDateMap);
        return dashboardVO;
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

    private Map<String, ShanzhuCategory> queryCategoryMap(List<ShanzhuGoal> goals) {
        List<String> categoryIds = goals.stream()
                .map(ShanzhuGoal::getCategoryId)
                .filter(categoryId -> categoryId != null && !categoryId.isBlank())
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

    private Map<String, LocalDate> queryGoalLastProgressDateMap() {
        QueryWrapper<ShanzhuGoalProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoalProgress::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoalProgress::getDelFlag, NORMAL_FLAG);
        return shanzhuGoalProgressMapper.selectList(queryWrapper).stream()
                .filter(progress -> progress.getGoalId() != null && progress.getRecordDate() != null)
                .collect(Collectors.toMap(
                        ShanzhuGoalProgress::getGoalId,
                        ShanzhuGoalProgress::getRecordDate,
                        (oldDate, newDate) -> oldDate.isAfter(newDate) ? oldDate : newDate
                ));
    }

    private void fillOverview(ShanzhuDashboardVO.OverviewStats overview,
                              List<ShanzhuGoal> goals,
                              List<ShanzhuTask> tasks,
                              Map<String, LocalDate> goalLastProgressDateMap) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        overview.setTotalGoalCount(goals.size());
        overview.setInProgressGoalCount(countGoalsByStatus(goals, IN_PROGRESS_STATUS));
        overview.setCompletedGoalCount(countGoalsByStatus(goals, COMPLETED_STATUS));
        overview.setPausedGoalCount(countGoalsByStatus(goals, PAUSED_STATUS));
        overview.setAbandonedGoalCount(countGoalsByStatus(goals, ABANDONED_STATUS));
        overview.setTotalTaskCount(tasks.size());
        overview.setPendingTaskCount(tasks.stream().filter(this::isUnfinishedTask).count());
        overview.setCompletedTaskCount(countTasksByStatus(tasks, COMPLETED_STATUS));
        overview.setOverdueGoalCount(goals.stream().filter(goal -> isOverdueGoal(goal, today)).count());
        overview.setOverdueTaskCount(tasks.stream().filter(task -> isOverdueTask(task, now)).count());
        overview.setLongTimeNoProgressGoalCount(goals.stream()
                .filter(goal -> isLongTimeNoProgressGoal(goal, goalLastProgressDateMap, today))
                .count());
        overview.setHighPriorityUnfinishedTaskCount(tasks.stream()
                .filter(task -> isUnfinishedTask(task) && HIGH_PRIORITY == safePriority(task.getPriority()))
                .count());
    }

    private long countGoalsByStatus(List<ShanzhuGoal> goals, String status) {
        return goals.stream().filter(goal -> status.equals(goal.getStatus())).count();
    }

    private long countTasksByStatus(List<ShanzhuTask> tasks, String status) {
        return tasks.stream().filter(task -> status.equals(task.getStatus())).count();
    }

    private void fillCategoryStats(ShanzhuDashboardVO dashboardVO,
                                   List<ShanzhuGoal> goals,
                                   List<ShanzhuTask> tasks,
                                   Map<String, ShanzhuCategory> categoryMap,
                                   Map<String, ShanzhuGoal> goalMap) {
        Map<String, List<ShanzhuGoal>> categoryIdToGoals = goals.stream()
                .collect(Collectors.groupingBy(goal -> goal.getCategoryId() == null ? "" : goal.getCategoryId()));

        List<ShanzhuDashboardVO.CategoryStats> categoryStats = categoryIdToGoals.entrySet().stream()
                .map(entry -> buildCategoryStats(entry.getKey(), entry.getValue(), tasks, categoryMap, goalMap))
                .sorted(Comparator.comparing(ShanzhuDashboardVO.CategoryStats::getGoalCount).reversed())
                .toList();
        dashboardVO.setCategoryStats(categoryStats);
    }

    private ShanzhuDashboardVO.CategoryStats buildCategoryStats(String categoryId,
                                                                List<ShanzhuGoal> goals,
                                                                List<ShanzhuTask> tasks,
                                                                Map<String, ShanzhuCategory> categoryMap,
                                                                Map<String, ShanzhuGoal> goalMap) {
        ShanzhuDashboardVO.CategoryStats categoryStats = new ShanzhuDashboardVO.CategoryStats();
        ShanzhuCategory category = categoryMap.get(categoryId);
        categoryStats.setCategoryId(categoryId);
        categoryStats.setCategoryName(category == null ? "未分类" : category.getName());
        categoryStats.setCategoryColor(category == null ? "" : category.getColor());
        categoryStats.setGoalCount(goals.size());
        categoryStats.setCompletedGoalCount(countGoalsByStatus(goals, COMPLETED_STATUS));

        List<ShanzhuTask> categoryTasks = tasks.stream()
                .filter(task -> {
                    ShanzhuGoal goal = goalMap.get(task.getGoalId());
                    String taskCategoryId = goal == null ? "" : goal.getCategoryId();
                    return categoryId.equals(taskCategoryId == null ? "" : taskCategoryId);
                })
                .toList();
        categoryStats.setTaskCount(categoryTasks.size());
        categoryStats.setCompletedTaskCount(countTasksByStatus(categoryTasks, COMPLETED_STATUS));
        return categoryStats;
    }

    private void fillTimeStats(ShanzhuDashboardVO.TimeStats timeStats, List<ShanzhuTask> tasks) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate monthStart = today.withDayOfMonth(1);

        timeStats.setTodayCompletedTaskCount(countCompletedTasksBetween(tasks, today, today));
        timeStats.setWeekCompletedTaskCount(countCompletedTasksBetween(tasks, weekStart, today));
        timeStats.setMonthCompletedTaskCount(countCompletedTasksBetween(tasks, monthStart, today));

        List<ShanzhuDashboardVO.DailyTaskCompletionStats> trend = today.minusDays(6).datesUntil(today.plusDays(1))
                .map(date -> {
                    ShanzhuDashboardVO.DailyTaskCompletionStats dailyStats = new ShanzhuDashboardVO.DailyTaskCompletionStats();
                    dailyStats.setDate(date);
                    dailyStats.setCompletedTaskCount(countCompletedTasksBetween(tasks, date, date));
                    return dailyStats;
                })
                .toList();
        timeStats.setRecentSevenDayTaskCompletionTrend(trend);
    }

    private long countCompletedTasksBetween(List<ShanzhuTask> tasks, LocalDate startDate, LocalDate endDate) {
        return tasks.stream()
                .filter(task -> COMPLETED_STATUS.equals(task.getStatus()))
                .filter(task -> task.getCompletedTime() != null)
                .filter(task -> {
                    LocalDate completedDate = task.getCompletedTime().toLocalDate();
                    return !completedDate.isBefore(startDate) && !completedDate.isAfter(endDate);
                })
                .count();
    }

    private void fillRiskStats(ShanzhuDashboardVO.RiskStats riskStats,
                               List<ShanzhuGoal> goals,
                               List<ShanzhuTask> tasks,
                               Map<String, ShanzhuCategory> categoryMap,
                               Map<String, ShanzhuGoal> goalMap,
                               Map<String, LocalDate> goalLastProgressDateMap) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        riskStats.setOverdueGoals(goals.stream()
                .filter(goal -> isOverdueGoal(goal, today))
                .sorted(Comparator.comparing(ShanzhuGoal::getDeadline))
                .limit(RISK_LIST_LIMIT)
                .map(goal -> buildRiskGoal(goal, categoryMap, goalLastProgressDateMap, today))
                .toList());
        riskStats.setOverdueTasks(tasks.stream()
                .filter(task -> isOverdueTask(task, now))
                .sorted(Comparator.comparing(ShanzhuTask::getDeadline))
                .limit(RISK_LIST_LIMIT)
                .map(task -> buildRiskTask(task, goalMap, categoryMap, today))
                .toList());
        riskStats.setLongTimeNoProgressGoals(goals.stream()
                .filter(goal -> isLongTimeNoProgressGoal(goal, goalLastProgressDateMap, today))
                .sorted(Comparator.comparing(goal -> getLastProgressDate(goal, goalLastProgressDateMap)))
                .limit(RISK_LIST_LIMIT)
                .map(goal -> buildRiskGoal(goal, categoryMap, goalLastProgressDateMap, today))
                .toList());
        riskStats.setHighPriorityUnfinishedTasks(tasks.stream()
                .filter(task -> isUnfinishedTask(task) && HIGH_PRIORITY == safePriority(task.getPriority()))
                .sorted(Comparator.comparing(ShanzhuTask::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(RISK_LIST_LIMIT)
                .map(task -> buildRiskTask(task, goalMap, categoryMap, today))
                .toList());
    }

    private ShanzhuDashboardVO.RiskGoal buildRiskGoal(ShanzhuGoal goal,
                                                      Map<String, ShanzhuCategory> categoryMap,
                                                      Map<String, LocalDate> goalLastProgressDateMap,
                                                      LocalDate today) {
        ShanzhuCategory category = categoryMap.get(goal.getCategoryId());
        LocalDate lastProgressDate = getLastProgressDate(goal, goalLastProgressDateMap);

        ShanzhuDashboardVO.RiskGoal riskGoal = new ShanzhuDashboardVO.RiskGoal();
        riskGoal.setId(goal.getId());
        riskGoal.setTitle(goal.getTitle());
        riskGoal.setCategoryId(goal.getCategoryId());
        riskGoal.setCategoryName(category == null ? "未分类" : category.getName());
        riskGoal.setCategoryColor(category == null ? "" : category.getColor());
        riskGoal.setStatus(goal.getStatus());
        riskGoal.setPriority(goal.getPriority());
        riskGoal.setProgress(goal.getProgress());
        riskGoal.setDeadline(goal.getDeadline());
        riskGoal.setOverdueDays(goal.getDeadline() == null ? 0 : ChronoUnit.DAYS.between(goal.getDeadline(), today));
        riskGoal.setLastProgressDate(lastProgressDate);
        riskGoal.setDaysSinceLastProgress(ChronoUnit.DAYS.between(lastProgressDate, today));
        return riskGoal;
    }

    private ShanzhuDashboardVO.RiskTask buildRiskTask(ShanzhuTask task,
                                                      Map<String, ShanzhuGoal> goalMap,
                                                      Map<String, ShanzhuCategory> categoryMap,
                                                      LocalDate today) {
        ShanzhuGoal goal = goalMap.get(task.getGoalId());
        ShanzhuCategory category = goal == null ? null : categoryMap.get(goal.getCategoryId());

        ShanzhuDashboardVO.RiskTask riskTask = new ShanzhuDashboardVO.RiskTask();
        riskTask.setId(task.getId());
        riskTask.setTitle(task.getTitle());
        riskTask.setGoalId(task.getGoalId());
        riskTask.setGoalTitle(goal == null ? "" : goal.getTitle());
        riskTask.setCategoryId(goal == null ? "" : goal.getCategoryId());
        riskTask.setCategoryName(category == null ? "未分类" : category.getName());
        riskTask.setCategoryColor(category == null ? "" : category.getColor());
        riskTask.setStatus(task.getStatus());
        riskTask.setPriority(task.getPriority());
        riskTask.setDeadline(task.getDeadline() == null ? null : task.getDeadline().toLocalDate());
        riskTask.setOverdueDays(task.getDeadline() == null ? 0 : ChronoUnit.DAYS.between(task.getDeadline().toLocalDate(), today));
        return riskTask;
    }

    private boolean isOverdueGoal(ShanzhuGoal goal, LocalDate today) {
        return isUnfinishedGoal(goal) && goal.getDeadline() != null && goal.getDeadline().isBefore(today);
    }

    private boolean isOverdueTask(ShanzhuTask task, LocalDateTime now) {
        return isUnfinishedTask(task) && task.getDeadline() != null && task.getDeadline().isBefore(now);
    }

    private boolean isLongTimeNoProgressGoal(ShanzhuGoal goal,
                                             Map<String, LocalDate> goalLastProgressDateMap,
                                             LocalDate today) {
        return isUnfinishedGoal(goal)
                && ChronoUnit.DAYS.between(getLastProgressDate(goal, goalLastProgressDateMap), today) >= LONG_TIME_NO_PROGRESS_DAYS;
    }

    private LocalDate getLastProgressDate(ShanzhuGoal goal, Map<String, LocalDate> goalLastProgressDateMap) {
        LocalDate lastProgressDate = goalLastProgressDateMap.get(goal.getId());
        if (lastProgressDate != null) {
            return lastProgressDate;
        }
        return goal.getCreateTime() == null ? LocalDate.now() : goal.getCreateTime().toLocalDate();
    }

    private boolean isUnfinishedGoal(ShanzhuGoal goal) {
        return !COMPLETED_STATUS.equals(goal.getStatus()) && !ABANDONED_STATUS.equals(goal.getStatus());
    }

    private boolean isUnfinishedTask(ShanzhuTask task) {
        return !COMPLETED_STATUS.equals(task.getStatus());
    }

    private int safePriority(Integer priority) {
        return priority == null ? 0 : priority;
    }
}
