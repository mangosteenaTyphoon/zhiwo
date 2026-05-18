package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuCategory;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.entity.ShanzhuTagRelation;
import com.shanzhu.entity.ShanzhuTask;
import com.shanzhu.mapper.ShanzhuCategoryMapper;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.mapper.ShanzhuTagRelationMapper;
import com.shanzhu.mapper.ShanzhuTaskMapper;
import com.shanzhu.model.dto.ShanzhuTagRelationQueryDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationSaveDTO;
import com.shanzhu.model.dto.ShanzhuTaskQueryDTO;
import com.shanzhu.model.dto.ShanzhuTaskSaveDTO;
import com.shanzhu.model.dto.ShanzhuTaskSortDTO;
import com.shanzhu.model.dto.ShanzhuTaskStatusDTO;
import com.shanzhu.model.vo.ShanzhuTaskVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuTagRelationService;
import com.shanzhu.service.ShanzhuTaskService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShanzhuTaskServiceImpl extends ServiceImpl<ShanzhuTaskMapper, ShanzhuTask> implements ShanzhuTaskService {

    private static final String DEFAULT_STATUS = "not_started";
    private static final String DELETED_FLAG = "1";
    private static final String NORMAL_FLAG = "0";
    private static final String COMPLETED_STATUS = "completed";
    private static final String TASK_TAG_BIZ_TYPE = "task";
    private static final String TODAY_QUERY_TYPE = "today";
    private static final String OVERDUE_QUERY_TYPE = "overdue";

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Resource
    private ShanzhuCategoryMapper shanzhuCategoryMapper;

    @Resource
    private ShanzhuTagRelationMapper shanzhuTagRelationMapper;

    @Resource
    private ShanzhuTagRelationService shanzhuTagRelationService;

    @Override
    public List<ShanzhuTaskVO> queryTaskList(ShanzhuTaskQueryDTO queryDTO) {
        QueryWrapper<ShanzhuTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTask::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTask::getDelFlag, NORMAL_FLAG)
                .eq(StringUtils.hasText(queryDTO.getGoalId()), ShanzhuTask::getGoalId, queryDTO.getGoalId())
                .eq(StringUtils.hasText(queryDTO.getSubGoalId()), ShanzhuTask::getSubGoalId, queryDTO.getSubGoalId())
                .eq(StringUtils.hasText(queryDTO.getStatus()), ShanzhuTask::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getPriority() != null, ShanzhuTask::getPriority, queryDTO.getPriority())
                .eq(queryDTO.getPlannedDate() != null, ShanzhuTask::getPlannedDate, queryDTO.getPlannedDate())
                .like(StringUtils.hasText(queryDTO.getKeyword()), ShanzhuTask::getTitle, queryDTO.getKeyword());

        applyGoalFilter(queryWrapper, queryDTO);
        applyTagFilter(queryWrapper, queryDTO);
        applyQueryTypeFilter(queryWrapper, queryDTO);

        queryWrapper.lambda()
                .orderByAsc(ShanzhuTask::getSortOrder)
                .orderByAsc(ShanzhuTask::getPlannedDate)
                .orderByAsc(ShanzhuTask::getDeadline)
                .orderByDesc(ShanzhuTask::getCreateTime);
        return convertToVOList(list(queryWrapper));
    }

    @Override
    public String saveTask(ShanzhuTaskSaveDTO saveDTO) {
        ShanzhuTask task = new ShanzhuTask();
        BeanUtils.copyProperties(saveDTO, task);
        task.setUserId(LoginUserContext.getUserId());
        task.setStatus(StringUtils.hasText(saveDTO.getStatus()) ? saveDTO.getStatus() : DEFAULT_STATUS);
        task.setPriority(saveDTO.getPriority() == null ? 2 : saveDTO.getPriority());
        task.setSortOrder(saveDTO.getSortOrder() == null ? 0 : saveDTO.getSortOrder());

        if (COMPLETED_STATUS.equals(task.getStatus()) && task.getCompletedTime() == null) {
            task.setCompletedTime(LocalDateTime.now());
        }

        if (StringUtils.hasText(task.getId())) {
            updateById(task);
        } else {
            save(task);
        }

        if (saveDTO.getTagIds() != null) {
            ShanzhuTagRelationSaveDTO relationSaveDTO = new ShanzhuTagRelationSaveDTO();
            relationSaveDTO.setBizType(TASK_TAG_BIZ_TYPE);
            relationSaveDTO.setBizId(task.getId());
            relationSaveDTO.setTagIds(saveDTO.getTagIds());
            shanzhuTagRelationService.saveRelations(relationSaveDTO);
        }
        return task.getId();
    }

    @Override
    public void deleteTask(String id) {
        ShanzhuTask task = new ShanzhuTask();
        task.setId(id);
        task.setDelFlag(DELETED_FLAG);
        updateById(task);
    }

    @Override
    public void updateStatus(ShanzhuTaskStatusDTO statusDTO) {
        ShanzhuTask task = new ShanzhuTask();
        task.setId(statusDTO.getId());
        task.setStatus(statusDTO.getStatus());
        if (COMPLETED_STATUS.equals(statusDTO.getStatus())) {
            task.setCompletedTime(LocalDateTime.now());
        }
        updateById(task);
    }

    @Override
    public void updateSort(ShanzhuTaskSortDTO sortDTO) {
        ShanzhuTask task = new ShanzhuTask();
        task.setId(sortDTO.getId());
        task.setSortOrder(sortDTO.getSortOrder());
        updateById(task);
    }

    private void applyGoalFilter(QueryWrapper<ShanzhuTask> queryWrapper, ShanzhuTaskQueryDTO queryDTO) {
        if (!StringUtils.hasText(queryDTO.getCategoryId())) {
            return;
        }

        QueryWrapper<ShanzhuGoal> goalQueryWrapper = new QueryWrapper<>();
        goalQueryWrapper.lambda()
                .select(ShanzhuGoal::getId)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getCategoryId, queryDTO.getCategoryId());
        List<String> goalIds = shanzhuGoalMapper.selectList(goalQueryWrapper).stream()
                .map(ShanzhuGoal::getId)
                .toList();

        if (goalIds.isEmpty()) {
            queryWrapper.lambda().eq(ShanzhuTask::getGoalId, "");
            return;
        }
        queryWrapper.lambda().in(ShanzhuTask::getGoalId, goalIds);
    }

    private void applyTagFilter(QueryWrapper<ShanzhuTask> queryWrapper, ShanzhuTaskQueryDTO queryDTO) {
        if (queryDTO.getTagIds() == null || queryDTO.getTagIds().isEmpty()) {
            return;
        }

        QueryWrapper<ShanzhuTagRelation> relationQueryWrapper = new QueryWrapper<>();
        relationQueryWrapper.lambda()
                .select(ShanzhuTagRelation::getBizId)
                .eq(ShanzhuTagRelation::getBizType, TASK_TAG_BIZ_TYPE)
                .eq(ShanzhuTagRelation::getUserId, LoginUserContext.getUserId())
                .in(ShanzhuTagRelation::getTagId, queryDTO.getTagIds());
        List<String> taskIds = shanzhuTagRelationMapper.selectList(relationQueryWrapper).stream()
                .map(ShanzhuTagRelation::getBizId)
                .distinct()
                .toList();

        if (taskIds.isEmpty()) {
            queryWrapper.lambda().eq(ShanzhuTask::getId, "");
            return;
        }
        queryWrapper.lambda().in(ShanzhuTask::getId, taskIds);
    }

    private void applyQueryTypeFilter(QueryWrapper<ShanzhuTask> queryWrapper, ShanzhuTaskQueryDTO queryDTO) {
        LocalDate today = LocalDate.now();
        if (TODAY_QUERY_TYPE.equals(queryDTO.getQueryType())) {
            queryWrapper.lambda().eq(ShanzhuTask::getPlannedDate, today);
        }
        if (OVERDUE_QUERY_TYPE.equals(queryDTO.getQueryType())) {
            queryWrapper.lambda()
                    .ne(ShanzhuTask::getStatus, COMPLETED_STATUS)
                    .lt(ShanzhuTask::getDeadline, LocalDateTime.now());
        }
    }

    private List<ShanzhuTaskVO> convertToVOList(List<ShanzhuTask> tasks) {
        List<ShanzhuTaskVO> taskVOList = new ArrayList<>();
        Map<String, ShanzhuGoal> goalMap = queryGoalMap(tasks);
        Map<String, ShanzhuCategory> categoryMap = queryCategoryMap(goalMap);

        tasks.forEach(task -> {
            ShanzhuTaskVO taskVO = new ShanzhuTaskVO();
            BeanUtils.copyProperties(task, taskVO);
            fillGoalInfo(taskVO, goalMap, categoryMap);
            fillTags(taskVO);
            taskVOList.add(taskVO);
        });
        return taskVOList;
    }

    private Map<String, ShanzhuGoal> queryGoalMap(List<ShanzhuTask> tasks) {
        List<String> goalIds = tasks.stream()
                .map(ShanzhuTask::getGoalId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (goalIds.isEmpty()) {
            return Map.of();
        }
        return shanzhuGoalMapper.selectBatchIds(goalIds).stream()
                .collect(Collectors.toMap(ShanzhuGoal::getId, goal -> goal));
    }

    private Map<String, ShanzhuCategory> queryCategoryMap(Map<String, ShanzhuGoal> goalMap) {
        List<String> categoryIds = goalMap.values().stream()
                .map(ShanzhuGoal::getCategoryId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return shanzhuCategoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(ShanzhuCategory::getId, category -> category));
    }

    private void fillGoalInfo(ShanzhuTaskVO taskVO, Map<String, ShanzhuGoal> goalMap, Map<String, ShanzhuCategory> categoryMap) {
        ShanzhuGoal goal = goalMap.get(taskVO.getGoalId());
        if (goal == null) {
            return;
        }

        taskVO.setGoalTitle(goal.getTitle());
        taskVO.setCategoryId(goal.getCategoryId());

        ShanzhuCategory category = categoryMap.get(goal.getCategoryId());
        if (category != null) {
            taskVO.setCategoryName(category.getName());
            taskVO.setCategoryColor(category.getColor());
        }
    }

    private void fillTags(ShanzhuTaskVO taskVO) {
        ShanzhuTagRelationQueryDTO relationQueryDTO = new ShanzhuTagRelationQueryDTO();
        relationQueryDTO.setBizType(TASK_TAG_BIZ_TYPE);
        relationQueryDTO.setBizId(taskVO.getId());
        taskVO.setTags(shanzhuTagRelationService.queryRelations(relationQueryDTO));
    }
}
