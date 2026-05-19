package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
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
import com.shanzhu.model.dto.ShanzhuHabitCheckinSaveDTO;
import com.shanzhu.model.vo.ShanzhuHabitCheckinVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuHabitCheckinService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShanzhuHabitCheckinServiceImpl extends ServiceImpl<ShanzhuHabitCheckinMapper, ShanzhuHabitCheckin> implements ShanzhuHabitCheckinService {

    private static final String DELETED_FLAG = "1";
    private static final String NORMAL_FLAG = "0";
    private static final String ACTIVE_STATUS = "active";
    private static final String CHECKED_STATUS = "checked";

    @Resource
    private ShanzhuHabitMapper shanzhuHabitMapper;

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Resource
    private ShanzhuSubGoalMapper shanzhuSubGoalMapper;

    @Override
    public String saveCheckin(ShanzhuHabitCheckinSaveDTO saveDTO) {
        ShanzhuHabit habit = queryCurrentUserActiveHabit(saveDTO.getHabitId());
        if (habit == null) {
            return null;
        }

        LocalDate checkinDate = saveDTO.getCheckinDate() == null ? LocalDate.now() : saveDTO.getCheckinDate();
        ShanzhuHabitCheckin oldCheckin = queryCurrentUserCheckinIncludeDeleted(saveDTO.getHabitId(), checkinDate);

        ShanzhuHabitCheckin checkin = new ShanzhuHabitCheckin();
        BeanUtils.copyProperties(saveDTO, checkin);
        checkin.setId(oldCheckin == null ? null : oldCheckin.getId());
        checkin.setUserId(LoginUserContext.getUserId());
        checkin.setHabitId(habit.getId());
        checkin.setGoalId(habit.getGoalId());
        checkin.setSubGoalId(habit.getSubGoalId());
        checkin.setCheckinDate(checkinDate);
        checkin.setUnit(habit.getUnit());
        checkin.setStatus(CHECKED_STATUS);
        checkin.setDelFlag(NORMAL_FLAG);

        if (oldCheckin == null) {
            save(checkin);
        } else {
            UpdateWrapper<ShanzhuHabitCheckin> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .eq(ShanzhuHabitCheckin::getId, oldCheckin.getId())
                    .eq(ShanzhuHabitCheckin::getUserId, LoginUserContext.getUserId());
            update(checkin, updateWrapper);
        }
        return checkin.getId();
    }

    @Override
    public void cancelCheckin(String id) {
        ShanzhuHabitCheckin checkin = new ShanzhuHabitCheckin();
        checkin.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuHabitCheckin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuHabitCheckin::getId, id)
                .eq(ShanzhuHabitCheckin::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabitCheckin::getDelFlag, NORMAL_FLAG);
        update(checkin, updateWrapper);
    }

    @Override
    public List<ShanzhuHabitCheckinVO> queryCheckins(ShanzhuHabitCheckinQueryDTO queryDTO) {
        QueryWrapper<ShanzhuHabitCheckin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.hasText(queryDTO.getHabitId()), ShanzhuHabitCheckin::getHabitId, queryDTO.getHabitId())
                .eq(ShanzhuHabitCheckin::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabitCheckin::getDelFlag, NORMAL_FLAG)
                .ge(queryDTO.getStartDate() != null, ShanzhuHabitCheckin::getCheckinDate, queryDTO.getStartDate())
                .le(queryDTO.getEndDate() != null, ShanzhuHabitCheckin::getCheckinDate, queryDTO.getEndDate())
                .orderByDesc(ShanzhuHabitCheckin::getCheckinDate)
                .orderByDesc(ShanzhuHabitCheckin::getCreateTime);
        return convertToVOList(list(queryWrapper));
    }

    private ShanzhuHabit queryCurrentUserActiveHabit(String habitId) {
        if (!StringUtils.hasText(habitId)) {
            return null;
        }

        QueryWrapper<ShanzhuHabit> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuHabit::getId, habitId)
                .eq(ShanzhuHabit::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabit::getStatus, ACTIVE_STATUS)
                .eq(ShanzhuHabit::getDelFlag, NORMAL_FLAG);
        return shanzhuHabitMapper.selectOne(queryWrapper);
    }

    private ShanzhuHabitCheckin queryCurrentUserCheckinIncludeDeleted(String habitId, LocalDate checkinDate) {
        QueryWrapper<ShanzhuHabitCheckin> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuHabitCheckin::getHabitId, habitId)
                .eq(ShanzhuHabitCheckin::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuHabitCheckin::getCheckinDate, checkinDate)
                .orderByDesc(ShanzhuHabitCheckin::getCreateTime);
        List<ShanzhuHabitCheckin> checkins = list(queryWrapper);
        return checkins.isEmpty() ? null : checkins.get(0);
    }

    private List<ShanzhuHabitCheckinVO> convertToVOList(List<ShanzhuHabitCheckin> checkins) {
        List<ShanzhuHabitCheckinVO> checkinVOList = new ArrayList<>();
        Map<String, ShanzhuHabit> habitMap = queryHabitMap(checkins);
        Map<String, ShanzhuGoal> goalMap = queryGoalMap(checkins);
        Map<String, ShanzhuSubGoal> subGoalMap = querySubGoalMap(checkins);

        checkins.forEach(checkin -> {
            ShanzhuHabitCheckinVO checkinVO = new ShanzhuHabitCheckinVO();
            BeanUtils.copyProperties(checkin, checkinVO);

            if (StringUtils.hasText(checkin.getHabitId())) {
                ShanzhuHabit habit = habitMap.get(checkin.getHabitId());
                if (habit != null) {
                    checkinVO.setHabitTitle(habit.getTitle());
                }
            }

            if (StringUtils.hasText(checkin.getGoalId())) {
                ShanzhuGoal goal = goalMap.get(checkin.getGoalId());
                if (goal != null) {
                    checkinVO.setGoalTitle(goal.getTitle());
                }
            }

            if (StringUtils.hasText(checkin.getSubGoalId())) {
                ShanzhuSubGoal subGoal = subGoalMap.get(checkin.getSubGoalId());
                if (subGoal != null) {
                    checkinVO.setSubGoalTitle(subGoal.getTitle());
                }
            }

            checkinVOList.add(checkinVO);
        });
        return checkinVOList;
    }

    private Map<String, ShanzhuHabit> queryHabitMap(List<ShanzhuHabitCheckin> checkins) {
        List<String> habitIds = checkins.stream()
                .map(ShanzhuHabitCheckin::getHabitId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (habitIds.isEmpty()) {
            return new HashMap<>();
        }

        QueryWrapper<ShanzhuHabit> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(ShanzhuHabit::getId, habitIds)
                .eq(ShanzhuHabit::getUserId, LoginUserContext.getUserId());
        return shanzhuHabitMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(ShanzhuHabit::getId, habit -> habit));
    }

    private Map<String, ShanzhuGoal> queryGoalMap(List<ShanzhuHabitCheckin> checkins) {
        List<String> goalIds = checkins.stream()
                .map(ShanzhuHabitCheckin::getGoalId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (goalIds.isEmpty()) {
            return new HashMap<>();
        }

        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(ShanzhuGoal::getId, goalIds)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
        return shanzhuGoalMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(ShanzhuGoal::getId, goal -> goal));
    }

    private Map<String, ShanzhuSubGoal> querySubGoalMap(List<ShanzhuHabitCheckin> checkins) {
        List<String> subGoalIds = checkins.stream()
                .map(ShanzhuHabitCheckin::getSubGoalId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (subGoalIds.isEmpty()) {
            return new HashMap<>();
        }

        QueryWrapper<ShanzhuSubGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .in(ShanzhuSubGoal::getId, subGoalIds)
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG);
        return shanzhuSubGoalMapper.selectList(queryWrapper).stream()
                .collect(Collectors.toMap(ShanzhuSubGoal::getId, subGoal -> subGoal));
    }
}
