package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanzhu.entity.ShanzhuHabit;
import com.shanzhu.entity.ShanzhuTask;
import com.shanzhu.entity.ShanzhuTodo;
import com.shanzhu.mapper.ShanzhuHabitMapper;
import com.shanzhu.mapper.ShanzhuTaskMapper;
import com.shanzhu.mapper.ShanzhuTodoMapper;
import com.shanzhu.model.vo.ShanzhuHabitTodayVO;
import com.shanzhu.model.vo.ShanzhuTaskVO;
import com.shanzhu.model.vo.ShanzhuTodayWorkVO;
import com.shanzhu.model.vo.ShanzhuTodoVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuHabitService;
import com.shanzhu.service.ShanzhuTaskService;
import com.shanzhu.service.ShanzhuTodayWorkService;
import com.shanzhu.service.ShanzhuTodoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShanzhuTodayWorkServiceImpl implements ShanzhuTodayWorkService {

    private static final String NORMAL_FLAG = "0";
    private static final String COMPLETED_STATUS = "completed";
    private static final String TODAY_STATUS = "today";
    private static final String DONE_STATUS = "done";

    @Resource
    private ShanzhuTaskService shanzhuTaskService;

    @Resource
    private ShanzhuHabitService shanzhuHabitService;

    @Resource
    private ShanzhuTodoService shanzhuTodoService;

    @Resource
    private ShanzhuTaskMapper shanzhuTaskMapper;

    @Resource
    private ShanzhuHabitMapper shanzhuHabitMapper;

    @Resource
    private ShanzhuTodoMapper shanzhuTodoMapper;

    @Override
    public ShanzhuTodayWorkVO queryTodayWork() {
        ShanzhuTodayWorkVO todayWorkVO = new ShanzhuTodayWorkVO();

        // 查询今日任务
        List<ShanzhuTaskVO> todayTasks = queryTodayTasks();
        todayWorkVO.setTasks(todayTasks);

        // 查询今日习惯
        List<ShanzhuHabitTodayVO> todayHabits = shanzhuHabitService.queryTodayHabits();
        todayWorkVO.setHabits(todayHabits);

        // 查询今日Todo
        List<ShanzhuTodoVO> todayTodos = queryTodayTodos();
        todayWorkVO.setTodos(todayTodos);

        // 组装概览统计
        ShanzhuTodayWorkVO.TodayOverview overview = new ShanzhuTodayWorkVO.TodayOverview();
        overview.setTaskTotalCount(todayTasks.size());
        overview.setTaskCompletedCount((int) todayTasks.stream()
                .filter(task -> COMPLETED_STATUS.equals(task.getStatus()))
                .count());
        overview.setHabitTotalCount(todayHabits.size());
        overview.setHabitCheckedCount((int) todayHabits.stream()
                .filter(ShanzhuHabitTodayVO::getTodayChecked)
                .count());
        overview.setTodoTotalCount(todayTodos.size());
        overview.setTodoCompletedCount((int) todayTodos.stream()
                .filter(todo -> DONE_STATUS.equals(todo.getStatus()))
                .count());
        todayWorkVO.setOverview(overview);

        return todayWorkVO;
    }

    private List<ShanzhuTaskVO> queryTodayTasks() {
        LocalDate today = LocalDate.now();
        QueryWrapper<ShanzhuTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTask::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTask::getDelFlag, NORMAL_FLAG)
                .eq(ShanzhuTask::getPlannedDate, today)
                .orderByAsc(ShanzhuTask::getPriority)
                .orderByAsc(ShanzhuTask::getSortOrder)
                .orderByDesc(ShanzhuTask::getCreateTime);
        List<ShanzhuTask> tasks = shanzhuTaskMapper.selectList(queryWrapper);
        return tasks.stream().map(task -> {
            ShanzhuTaskVO vo = new ShanzhuTaskVO();
            org.springframework.beans.BeanUtils.copyProperties(task, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private List<ShanzhuTodoVO> queryTodayTodos() {
        LocalDate today = LocalDate.now();
        QueryWrapper<ShanzhuTodo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTodo::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTodo::getDelFlag, NORMAL_FLAG)
                .and(wrapper -> wrapper
                        .eq(ShanzhuTodo::getStatus, TODAY_STATUS)
                        .or()
                        .eq(ShanzhuTodo::getPlannedDate, today)
                )
                .orderByAsc(ShanzhuTodo::getPriority)
                .orderByDesc(ShanzhuTodo::getCreateTime);
        List<ShanzhuTodo> todos = shanzhuTodoMapper.selectList(queryWrapper);
        return todos.stream().map(todo -> {
            ShanzhuTodoVO vo = new ShanzhuTodoVO();
            org.springframework.beans.BeanUtils.copyProperties(todo, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
