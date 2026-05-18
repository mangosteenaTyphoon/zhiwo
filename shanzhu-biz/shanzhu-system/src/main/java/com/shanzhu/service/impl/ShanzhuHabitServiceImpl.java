package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.entity.ShanzhuHabit;
import com.shanzhu.entity.ShanzhuHabitCheckin;
import com.shanzhu.entity.ShanzhuSubGoal;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.mapper.ShanzhuHabitCheckinMapper;
import com.shanzhu.mapper.ShanzhuHabitMapper;
import com.shanzhu.mapper.ShanzhuSubGoalMapper;
import com.shanzhu.model.dto.ShanzhuHabitCheckinQueryDTO;
import com.shanzhu.model.dto.ShanzhuHabitQueryDTO;
import com.shanzhu.model.dto.ShanzhuHabitSaveDTO;
import com.shanzhu.model.vo.ShanzhuHabitCheckinVO;
import com.shanzhu.model.vo.ShanzhuHabitStatsVO;
import com.shanzhu.model.vo.ShanzhuHabitTodayVO;
import com.shanzhu.model.vo.ShanzhuHabitVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuHabitCheckinService;
import com.shanzhu.service.ShanzhuHabitService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShanzhuHabitServiceImpl extends ServiceImpl<ShanzhuHabitMapper, ShanzhuHabit> implements ShanzhuHabitService {

    private static final String DELETED_FLAG = "1";
    private static final String NORMAL_FLAG = "0";
    private static final String ACTIVE_STATUS = "active";
    private static final String PAUSED_STATUS = "paused";
    private static final String DAILY_FREQUENCY = "daily";
    private static final String WEEKLY_FREQUENCY = "weekly";
    private static final String MONTHLY_FREQUENCY = "monthly";
    private static final int DEFAULT_TARGET_COUNT = 1;
    private static final int DEFAULT_SORT_ORDER = 0;
    private static final int RATE_SCALE = 2;
    private static final int PERCENT_MULTIPLIER = 100;
    private static final int RECENT_CHECKIN_DAYS = 7;

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Resource
    private ShanzhuSubGoalMapper shanzhuSubGoalMapper;

    @Resource
    private ShanzhuHabitCheckinMapper shanzhuHabitCheckinMapper;

    @Resource
    private ShanzhuHabitCheckinService shanzhuHabitCheckinService;

    @Override
    public IPage<ShanzhuHabitVO> queryHabitPage(ShanzhuHabitQueryDTO queryDTO) {
        IPage<ShanzhuHabit> habitPage = new Page<>(getPageNum(queryDTO), getPageSize(queryDTO));
        IPage<ShanzhuHabit> pageResult = page(habitPage, buildQueryWrapper(queryDTO));
        IPage<ShanzhuHabitVO> habitVOPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        habitVOPage.setRecords(convertToVOList(pageResult.getRecords()));
        return habitVOPage;
    }

    @Override
    public List<ShanzhuHabitVO> queryHabitList(ShanzhuHabitQueryDTO queryDTO) {
        return convertToVOList(list(buildQueryWrapper(queryDTO)));
    }

    @Override
    public String saveHabit(ShanzhuHabitSaveDTO saveDTO) {
        ShanzhuHabit oldHabit = queryCurrentUserHabit(saveDTO.getId());
        if (StringUtils.hasText(saveDTO.getId()) && oldHabit == null) {
            return saveDTO.getId();
        }
        if (StringUtils.hasText(saveDTO.getGoalId()) && !isCurrentUserGoal(saveDTO.getGoalId())) {
            return saveDTO.getId();
        }
        if (StringUtils.hasText(saveDTO.getSubGoalId()) && !isCurrentUserSubGoal(saveDTO.getSubGoalId(), saveDTO.getGoalId())) {
            return saveDTO.getId();
        }

        ShanzhuHabit habit = new ShanzhuHabit();
        BeanUtils.copyProperties(saveDTO, habit);
        habit.setUserId(LoginUserContext.getUserId());
        habit.setFrequencyType(StringUtils.hasText(saveDTO.getFrequencyType()) ? saveDTO.getFrequencyType() : DAILY_FREQUENCY);
        habit.setTargetCount(saveDTO.getTargetCount() == null || saveDTO.getTargetCount() < 1 ? DEFAULT_TARGET_COUNT : saveDTO.getTargetCount());
        habit.setStartDate(saveDTO.getStartDate() == null ? LocalDate.now() : saveDTO.getStartDate());
        habit.setStatus(StringUtils.hasText(saveDTO.getStatus()) ? saveDTO.getStatus() : ACTIVE_STATUS);
        habit.setSortOrder(saveDTO.getSortOrder() == null ? DEFAULT_SORT_ORDER : saveDTO.getSortOrder());

        if (StringUtils.hasText(habit.getId())) {
            UpdateWrapper<ShanzhuHabit> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .eq(ShanzhuHabit::getId, habit.getId())
                    .eq(ShanzhuHabit::getUserId, LoginUserContext.getUserId())
                    .eq(ShanzhuHabit::getDelFlag, NORMAL_FLAG);
            update(habit, updateWrapper);
        } else {
            save(habit);
        }
        return habit.getId();
    }

    @Override
    public void deleteHabit(String id) {
        ShanzhuHabit habit = new ShanzhuHabit();
        habit.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuHabit> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuHabit::getId, id)
                .eq(ShanzhuHabit::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabit::getDelFlag, NORMAL_FLAG);
        update(habit, updateWrapper);
    }

    @Override
    public void pauseHabit(String id) {
        updateHabitStatus(id, PAUSED_STATUS);
    }

    @Override
    public void resumeHabit(String id) {
        updateHabitStatus(id, ACTIVE_STATUS);
    }

    @Override
    public List<ShanzhuHabitTodayVO> queryTodayHabits() {
        List<ShanzhuHabit> habits = list(buildActiveHabitQueryWrapper(LocalDate.now()));
        Map<String, ShanzhuGoal> goalMap = queryGoalMap(habits);
        Map<String, ShanzhuSubGoal> subGoalMap = querySubGoalMap(habits);
        Map<String, ShanzhuHabitCheckin> todayCheckinMap = queryTodayCheckinMap(habits);

        List<ShanzhuHabitTodayVO> todayVOList = new ArrayList<>();
        habits.forEach(habit -> {
            ShanzhuHabitTodayVO todayVO = new ShanzhuHabitTodayVO();
            todayVO.setId(habit.getId());
            todayVO.setTitle(habit.getTitle());
            todayVO.setTargetValue(habit.getTargetValue());
            todayVO.setUnit(habit.getUnit());
            todayVO.setGoalId(habit.getGoalId());
            todayVO.setSubGoalId(habit.getSubGoalId());

            ShanzhuGoal goal = goalMap.get(habit.getGoalId());
            if (goal != null) {
                todayVO.setGoalTitle(goal.getTitle());
            }

            ShanzhuSubGoal subGoal = subGoalMap.get(habit.getSubGoalId());
            if (subGoal != null) {
                todayVO.setSubGoalTitle(subGoal.getTitle());
            }

            ShanzhuHabitCheckin todayCheckin = todayCheckinMap.get(habit.getId());
            todayVO.setTodayChecked(todayCheckin != null);
            if (todayCheckin != null) {
                todayVO.setTodayCheckinId(todayCheckin.getId());
                todayVO.setActualValue(todayCheckin.getActualValue());
                todayVO.setNote(todayCheckin.getNote());
            }
            todayVOList.add(todayVO);
        });
        return todayVOList;
    }

    @Override
    public ShanzhuHabitStatsVO queryHabitStats() {
        List<ShanzhuHabit> activeHabits = list(buildActiveHabitQueryWrapper(LocalDate.now()));
        Map<String, ShanzhuHabitCheckin> todayCheckinMap = queryTodayCheckinMap(activeHabits);

        int totalTargetCount = 0;
        int totalCheckedCount = 0;
        int completedHabitCount = 0;
        int maxContinuousDays = 0;

        for (ShanzhuHabit habit : activeHabits) {
            int periodTargetCount = calculateCurrentPeriodTargetCount(habit);
            int periodCheckedCount = countCheckins(habit.getId(), getCurrentPeriodStart(habit), getCurrentPeriodEnd(habit));
            totalTargetCount += periodTargetCount;
            totalCheckedCount += periodCheckedCount;
            if (periodTargetCount > 0 && periodCheckedCount >= periodTargetCount) {
                completedHabitCount++;
            }
            maxContinuousDays = Math.max(maxContinuousDays, calculateContinuousDays(habit.getId(), habit.getFrequencyType()));
        }

        ShanzhuHabitStatsVO statsVO = new ShanzhuHabitStatsVO();
        statsVO.setTodayTotalCount(activeHabits.size());
        statsVO.setTodayCheckedCount(todayCheckinMap.size());
        statsVO.setCurrentPeriodHabitCount(activeHabits.size());
        statsVO.setCurrentPeriodCompletedHabitCount(completedHabitCount);
        statsVO.setCurrentPeriodUncompletedHabitCount(Math.max(activeHabits.size() - completedHabitCount, 0));
        statsVO.setCurrentPeriodCompletionRate(calculateRate(totalCheckedCount, totalTargetCount));
        statsVO.setMaxContinuousDays(maxContinuousDays);
        return statsVO;
    }

    private QueryWrapper<ShanzhuHabit> buildQueryWrapper(ShanzhuHabitQueryDTO queryDTO) {
        QueryWrapper<ShanzhuHabit> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuHabit::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabit::getDelFlag, NORMAL_FLAG)
                .eq(StringUtils.hasText(queryDTO.getGoalId()), ShanzhuHabit::getGoalId, queryDTO.getGoalId())
                .eq(StringUtils.hasText(queryDTO.getSubGoalId()), ShanzhuHabit::getSubGoalId, queryDTO.getSubGoalId())
                .eq(StringUtils.hasText(queryDTO.getStatus()), ShanzhuHabit::getStatus, queryDTO.getStatus())
                .eq(StringUtils.hasText(queryDTO.getFrequencyType()), ShanzhuHabit::getFrequencyType, queryDTO.getFrequencyType())
                .like(StringUtils.hasText(queryDTO.getKeyword()), ShanzhuHabit::getTitle, queryDTO.getKeyword())
                .orderByAsc(ShanzhuHabit::getSortOrder)
                .orderByDesc(ShanzhuHabit::getCreateTime);
        return queryWrapper;
    }

    private QueryWrapper<ShanzhuHabit> buildActiveHabitQueryWrapper(LocalDate today) {
        QueryWrapper<ShanzhuHabit> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuHabit::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabit::getDelFlag, NORMAL_FLAG)
                .eq(ShanzhuHabit::getStatus, ACTIVE_STATUS)
                .le(ShanzhuHabit::getStartDate, today)
                .and(wrapper -> wrapper.isNull(ShanzhuHabit::getEndDate).or().ge(ShanzhuHabit::getEndDate, today))
                .orderByAsc(ShanzhuHabit::getSortOrder)
                .orderByDesc(ShanzhuHabit::getCreateTime);
        return queryWrapper;
    }

    private long getPageNum(ShanzhuHabitQueryDTO queryDTO) {
        return queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1 ? 1L : queryDTO.getPageNum();
    }

    private long getPageSize(ShanzhuHabitQueryDTO queryDTO) {
        return queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1 ? 10L : queryDTO.getPageSize();
    }

    private void updateHabitStatus(String id, String status) {
        ShanzhuHabit habit = new ShanzhuHabit();
        habit.setStatus(status);
        UpdateWrapper<ShanzhuHabit> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuHabit::getId, id)
                .eq(ShanzhuHabit::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabit::getDelFlag, NORMAL_FLAG);
        update(habit, updateWrapper);
    }

    private ShanzhuHabit queryCurrentUserHabit(String habitId) {
        if (!StringUtils.hasText(habitId)) {
            return null;
        }

        QueryWrapper<ShanzhuHabit> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuHabit::getId, habitId)
                .eq(ShanzhuHabit::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabit::getDelFlag, NORMAL_FLAG);
        return getOne(queryWrapper);
    }

    private boolean isCurrentUserGoal(String goalId) {
        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoal::getId, goalId)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
        return shanzhuGoalMapper.selectCount(queryWrapper) > 0;
    }

    private boolean isCurrentUserSubGoal(String subGoalId, String goalId) {
        QueryWrapper<ShanzhuSubGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuSubGoal::getId, subGoalId)
                .eq(StringUtils.hasText(goalId), ShanzhuSubGoal::getGoalId, goalId)
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG);
        return shanzhuSubGoalMapper.selectCount(queryWrapper) > 0;
    }

    private List<ShanzhuHabitVO> convertToVOList(List<ShanzhuHabit> habits) {
        List<ShanzhuHabitVO> habitVOList = new ArrayList<>();
        Map<String, ShanzhuGoal> goalMap = queryGoalMap(habits);
        Map<String, ShanzhuSubGoal> subGoalMap = querySubGoalMap(habits);
        Map<String, ShanzhuHabitCheckin> todayCheckinMap = queryTodayCheckinMap(habits);

        habits.forEach(habit -> {
            ShanzhuHabitVO habitVO = new ShanzhuHabitVO();
            BeanUtils.copyProperties(habit, habitVO);

            ShanzhuGoal goal = goalMap.get(habit.getGoalId());
            if (goal != null) {
                habitVO.setGoalTitle(goal.getTitle());
            }

            ShanzhuSubGoal subGoal = subGoalMap.get(habit.getSubGoalId());
            if (subGoal != null) {
                habitVO.setSubGoalTitle(subGoal.getTitle());
            }

            ShanzhuHabitCheckin todayCheckin = todayCheckinMap.get(habit.getId());
            habitVO.setTodayChecked(todayCheckin != null);
            if (todayCheckin != null) {
                habitVO.setTodayCheckinId(todayCheckin.getId());
            }

            LocalDate periodStart = getCurrentPeriodStart(habit);
            LocalDate periodEnd = getCurrentPeriodEnd(habit);
            int periodCheckedCount = countCheckins(habit.getId(), periodStart, periodEnd);
            int periodTargetCount = calculateCurrentPeriodTargetCount(habit);
            habitVO.setCurrentPeriodCheckedCount(periodCheckedCount);
            habitVO.setCurrentPeriodTargetCount(periodTargetCount);
            habitVO.setCurrentPeriodCompletionRate(calculateRate(periodCheckedCount, periodTargetCount));
            habitVO.setContinuousDays(calculateContinuousDays(habit.getId(), habit.getFrequencyType()));
            habitVO.setRecentCheckins(queryRecentCheckins(habit.getId()));
            habitVOList.add(habitVO);
        });
        return habitVOList;
    }

    private Map<String, ShanzhuGoal> queryGoalMap(List<ShanzhuHabit> habits) {
        List<String> goalIds = habits.stream()
                .map(ShanzhuHabit::getGoalId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (goalIds.isEmpty()) {
            return Map.of();
        }

        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(ShanzhuGoal::getId, goalIds)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
        return shanzhuGoalMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(ShanzhuGoal::getId, goal -> goal));
    }

    private Map<String, ShanzhuSubGoal> querySubGoalMap(List<ShanzhuHabit> habits) {
        List<String> subGoalIds = habits.stream()
                .map(ShanzhuHabit::getSubGoalId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (subGoalIds.isEmpty()) {
            return Map.of();
        }

        QueryWrapper<ShanzhuSubGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(ShanzhuSubGoal::getId, subGoalIds)
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG);
        return shanzhuSubGoalMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(ShanzhuSubGoal::getId, subGoal -> subGoal));
    }

    private Map<String, ShanzhuHabitCheckin> queryTodayCheckinMap(List<ShanzhuHabit> habits) {
        List<String> habitIds = habits.stream()
                .map(ShanzhuHabit::getId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (habitIds.isEmpty()) {
            return Map.of();
        }

        QueryWrapper<ShanzhuHabitCheckin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(ShanzhuHabitCheckin::getHabitId, habitIds)
                .eq(ShanzhuHabitCheckin::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabitCheckin::getCheckinDate, LocalDate.now())
                .eq(ShanzhuHabitCheckin::getDelFlag, NORMAL_FLAG);
        return shanzhuHabitCheckinMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(ShanzhuHabitCheckin::getHabitId, checkin -> checkin));
    }

    private List<ShanzhuHabitCheckinVO> queryRecentCheckins(String habitId) {
        ShanzhuHabitCheckinQueryDTO queryDTO = new ShanzhuHabitCheckinQueryDTO();
        queryDTO.setHabitId(habitId);
        queryDTO.setStartDate(LocalDate.now().minusDays(RECENT_CHECKIN_DAYS - 1L));
        queryDTO.setEndDate(LocalDate.now());
        return shanzhuHabitCheckinService.queryCheckins(queryDTO);
    }

    private int countCheckins(String habitId, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<ShanzhuHabitCheckin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuHabitCheckin::getHabitId, habitId)
                .eq(ShanzhuHabitCheckin::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabitCheckin::getDelFlag, NORMAL_FLAG)
                .ge(ShanzhuHabitCheckin::getCheckinDate, startDate)
                .le(ShanzhuHabitCheckin::getCheckinDate, endDate);
        return Math.toIntExact(shanzhuHabitCheckinMapper.selectCount(queryWrapper));
    }

    private LocalDate getCurrentPeriodStart(ShanzhuHabit habit) {
        LocalDate today = LocalDate.now();
        LocalDate periodStart;
        if (MONTHLY_FREQUENCY.equals(habit.getFrequencyType())) {
            periodStart = today.withDayOfMonth(1);
        } else {
            periodStart = today.with(DayOfWeek.MONDAY);
        }
        if (habit.getStartDate() != null && habit.getStartDate().isAfter(periodStart)) {
            return habit.getStartDate();
        }
        return periodStart;
    }

    private LocalDate getCurrentPeriodEnd(ShanzhuHabit habit) {
        LocalDate today = LocalDate.now();
        LocalDate periodEnd;
        if (MONTHLY_FREQUENCY.equals(habit.getFrequencyType())) {
            periodEnd = today.withDayOfMonth(today.lengthOfMonth());
        } else {
            periodEnd = today.with(DayOfWeek.SUNDAY);
        }
        if (habit.getEndDate() != null && habit.getEndDate().isBefore(periodEnd)) {
            return habit.getEndDate();
        }
        return periodEnd;
    }

    private int calculateCurrentPeriodTargetCount(ShanzhuHabit habit) {
        if (DAILY_FREQUENCY.equals(habit.getFrequencyType())) {
            long activeDays = ChronoUnit.DAYS.between(getCurrentPeriodStart(habit), getCurrentPeriodEnd(habit)) + 1;
            return Math.max(Math.toIntExact(activeDays), 0);
        }
        return habit.getTargetCount() == null || habit.getTargetCount() < 1 ? DEFAULT_TARGET_COUNT : habit.getTargetCount();
    }

    private BigDecimal calculateRate(int checkedCount, int targetCount) {
        if (targetCount <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(checkedCount)
                .multiply(BigDecimal.valueOf(PERCENT_MULTIPLIER))
                .divide(BigDecimal.valueOf(targetCount), RATE_SCALE, RoundingMode.HALF_UP);
    }

    private int calculateContinuousDays(String habitId, String frequencyType) {
        if (!DAILY_FREQUENCY.equals(frequencyType)) {
            return 0;
        }

        List<LocalDate> checkinDates = queryDailyCheckinDates(habitId);
        if (checkinDates.isEmpty()) {
            return 0;
        }

        LocalDate cursorDate = checkinDates.contains(LocalDate.now()) ? LocalDate.now() : LocalDate.now().minusDays(1);
        int continuousDays = 0;
        while (checkinDates.contains(cursorDate)) {
            continuousDays++;
            cursorDate = cursorDate.minusDays(1);
        }
        return continuousDays;
    }

    private List<LocalDate> queryDailyCheckinDates(String habitId) {
        QueryWrapper<ShanzhuHabitCheckin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .select(ShanzhuHabitCheckin::getCheckinDate)
                .eq(ShanzhuHabitCheckin::getHabitId, habitId)
                .eq(ShanzhuHabitCheckin::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabitCheckin::getDelFlag, NORMAL_FLAG)
                .le(ShanzhuHabitCheckin::getCheckinDate, LocalDate.now())
                .orderByDesc(ShanzhuHabitCheckin::getCheckinDate);
        return shanzhuHabitCheckinMapper.selectList(queryWrapper).stream()
                .map(ShanzhuHabitCheckin::getCheckinDate)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }
}
