package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.entity.ShanzhuSubGoal;
import com.shanzhu.entity.ShanzhuTodo;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.mapper.ShanzhuSubGoalMapper;
import com.shanzhu.mapper.ShanzhuTodoMapper;
import com.shanzhu.model.dto.ShanzhuTodoQueryDTO;
import com.shanzhu.model.dto.ShanzhuTodoSaveDTO;
import com.shanzhu.model.vo.ShanzhuTodoVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuTodoService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShanzhuTodoServiceImpl extends ServiceImpl<ShanzhuTodoMapper, ShanzhuTodo> implements ShanzhuTodoService {

    private static final String DELETED_FLAG = "1";
    private static final String NORMAL_FLAG = "0";
    private static final String INBOX_STATUS = "inbox";
    private static final String TODAY_STATUS = "today";
    private static final String DONE_STATUS = "done";
    private static final String ARCHIVED_STATUS = "archived";
    private static final String CONVERTED_STATUS = "converted";
    private static final int DEFAULT_SORT_ORDER = 0;

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Resource
    private ShanzhuSubGoalMapper shanzhuSubGoalMapper;

    @Override
    public IPage<ShanzhuTodoVO> queryTodoPage(ShanzhuTodoQueryDTO queryDTO) {
        IPage<ShanzhuTodo> todoPage = new Page<>(getPageNum(queryDTO), getPageSize(queryDTO));
        IPage<ShanzhuTodo> pageResult = page(todoPage, buildQueryWrapper(queryDTO));
        IPage<ShanzhuTodoVO> todoVOPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        todoVOPage.setRecords(convertToVOList(pageResult.getRecords()));
        return todoVOPage;
    }

    @Override
    public List<ShanzhuTodoVO> queryTodoList(ShanzhuTodoQueryDTO queryDTO) {
        return convertToVOList(list(buildQueryWrapper(queryDTO)));
    }

    @Override
    public String saveTodo(ShanzhuTodoSaveDTO saveDTO) {
        ShanzhuTodo oldTodo = queryCurrentUserTodo(saveDTO.getId());
        if (StringUtils.hasText(saveDTO.getId()) && oldTodo == null) {
            return saveDTO.getId();
        }
        if (StringUtils.hasText(saveDTO.getGoalId()) && !isCurrentUserGoal(saveDTO.getGoalId())) {
            return saveDTO.getId();
        }
        if (StringUtils.hasText(saveDTO.getSubGoalId()) && !isCurrentUserSubGoal(saveDTO.getSubGoalId(), saveDTO.getGoalId())) {
            return saveDTO.getId();
        }

        ShanzhuTodo todo = new ShanzhuTodo();
        BeanUtils.copyProperties(saveDTO, todo);
        todo.setUserId(LoginUserContext.getUserId());
        todo.setStatus(StringUtils.hasText(saveDTO.getStatus()) ? saveDTO.getStatus() : INBOX_STATUS);
        todo.setSortOrder(saveDTO.getSortOrder() == null ? DEFAULT_SORT_ORDER : saveDTO.getSortOrder());

        if (StringUtils.hasText(todo.getId())) {
            UpdateWrapper<ShanzhuTodo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .eq(ShanzhuTodo::getId, todo.getId())
                    .eq(ShanzhuTodo::getUserId, LoginUserContext.getUserId())
                    .eq(ShanzhuTodo::getDelFlag, NORMAL_FLAG);
            update(todo, updateWrapper);
        } else {
            save(todo);
        }
        return todo.getId();
    }

    @Override
    public void deleteTodo(String id) {
        ShanzhuTodo todo = new ShanzhuTodo();
        todo.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuTodo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuTodo::getId, id)
                .eq(ShanzhuTodo::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTodo::getDelFlag, NORMAL_FLAG);
        update(todo, updateWrapper);
    }

    @Override
    public void completeTodo(String id) {
        ShanzhuTodo todo = new ShanzhuTodo();
        todo.setStatus(DONE_STATUS);
        todo.setCompletedTime(LocalDateTime.now());
        updateTodoById(id, todo);
    }

    @Override
    public void uncompleteTodo(String id) {
        ShanzhuTodo todo = new ShanzhuTodo();
        todo.setStatus(INBOX_STATUS);
        todo.setCompletedTime(null);
        updateTodoById(id, todo);
    }

    @Override
    public void moveToToday(String id) {
        ShanzhuTodo todo = new ShanzhuTodo();
        todo.setStatus(TODAY_STATUS);
        updateTodoById(id, todo);
    }

    @Override
    public void moveToInbox(String id) {
        ShanzhuTodo todo = new ShanzhuTodo();
        todo.setStatus(INBOX_STATUS);
        updateTodoById(id, todo);
    }

    @Override
    public void archiveTodo(String id) {
        ShanzhuTodo todo = new ShanzhuTodo();
        todo.setStatus(ARCHIVED_STATUS);
        updateTodoById(id, todo);
    }

    private void updateTodoById(String id, ShanzhuTodo todo) {
        UpdateWrapper<ShanzhuTodo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuTodo::getId, id)
                .eq(ShanzhuTodo::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTodo::getDelFlag, NORMAL_FLAG);
        update(todo, updateWrapper);
    }

    private QueryWrapper<ShanzhuTodo> buildQueryWrapper(ShanzhuTodoQueryDTO queryDTO) {
        QueryWrapper<ShanzhuTodo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTodo::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTodo::getDelFlag, NORMAL_FLAG);

        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.lambda().eq(ShanzhuTodo::getStatus, queryDTO.getStatus());
        }
        if (StringUtils.hasText(queryDTO.getGoalId())) {
            queryWrapper.lambda().eq(ShanzhuTodo::getGoalId, queryDTO.getGoalId());
        }
        if (StringUtils.hasText(queryDTO.getSubGoalId())) {
            queryWrapper.lambda().eq(ShanzhuTodo::getSubGoalId, queryDTO.getSubGoalId());
        }
        if (queryDTO.getPriority() != null) {
            queryWrapper.lambda().eq(ShanzhuTodo::getPriority, queryDTO.getPriority());
        }
        if (queryDTO.getPlannedDate() != null) {
            queryWrapper.lambda().eq(ShanzhuTodo::getPlannedDate, queryDTO.getPlannedDate());
        }
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.lambda().like(ShanzhuTodo::getTitle, queryDTO.getKeyword());
        }
        queryWrapper.lambda().orderByDesc(ShanzhuTodo::getCreateTime);
        return queryWrapper;
    }

    private List<ShanzhuTodoVO> convertToVOList(List<ShanzhuTodo> todoList) {
        if (todoList == null || todoList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, ShanzhuGoal> goalMap = queryGoalMap(todoList);
        Map<String, ShanzhuSubGoal> subGoalMap = querySubGoalMap(todoList);

        return todoList.stream().map(todo -> {
            ShanzhuTodoVO vo = new ShanzhuTodoVO();
            BeanUtils.copyProperties(todo, vo);
            if (StringUtils.hasText(todo.getGoalId())) {
                ShanzhuGoal goal = goalMap.get(todo.getGoalId());
                if (goal != null) {
                    vo.setGoalTitle(goal.getTitle());
                }
            }
            if (StringUtils.hasText(todo.getSubGoalId())) {
                ShanzhuSubGoal subGoal = subGoalMap.get(todo.getSubGoalId());
                if (subGoal != null) {
                    vo.setSubGoalTitle(subGoal.getTitle());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<String, ShanzhuGoal> queryGoalMap(List<ShanzhuTodo> todoList) {
        List<String> goalIds = todoList.stream()
                .map(ShanzhuTodo::getGoalId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (goalIds.isEmpty()) {
            return new HashMap<>();
        }
        List<ShanzhuGoal> goals = shanzhuGoalMapper.selectBatchIds(goalIds);
        return goals.stream().collect(Collectors.toMap(ShanzhuGoal::getId, goal -> goal));
    }

    private Map<String, ShanzhuSubGoal> querySubGoalMap(List<ShanzhuTodo> todoList) {
        List<String> subGoalIds = todoList.stream()
                .map(ShanzhuTodo::getSubGoalId)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (subGoalIds.isEmpty()) {
            return new HashMap<>();
        }
        List<ShanzhuSubGoal> subGoals = shanzhuSubGoalMapper.selectBatchIds(subGoalIds);
        return subGoals.stream().collect(Collectors.toMap(ShanzhuSubGoal::getId, subGoal -> subGoal));
    }

    private ShanzhuTodo queryCurrentUserTodo(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        QueryWrapper<ShanzhuTodo> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTodo::getId, id)
                .eq(ShanzhuTodo::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTodo::getDelFlag, NORMAL_FLAG);
        return getOne(queryWrapper);
    }

    private boolean isCurrentUserGoal(String goalId) {
        ShanzhuGoal goal = shanzhuGoalMapper.selectById(goalId);
        return goal != null && goal.getUserId().equals(LoginUserContext.getUserId());
    }

    private boolean isCurrentUserSubGoal(String subGoalId, String goalId) {
        ShanzhuSubGoal subGoal = shanzhuSubGoalMapper.selectById(subGoalId);
        if (subGoal == null) {
            return false;
        }
        if (!subGoal.getUserId().equals(LoginUserContext.getUserId())) {
            return false;
        }
        return !StringUtils.hasText(goalId) || goalId.equals(subGoal.getGoalId());
    }

    private long getPageNum(ShanzhuTodoQueryDTO queryDTO) {
        return queryDTO.getPageNum() != null && queryDTO.getPageNum() > 0 ? queryDTO.getPageNum() : 1;
    }

    private long getPageSize(ShanzhuTodoQueryDTO queryDTO) {
        return queryDTO.getPageSize() != null && queryDTO.getPageSize() > 0 ? queryDTO.getPageSize() : 10;
    }
}
