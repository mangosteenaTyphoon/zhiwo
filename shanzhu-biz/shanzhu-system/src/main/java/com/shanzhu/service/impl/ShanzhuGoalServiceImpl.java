package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuCategory;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.entity.ShanzhuGoalProgress;
import com.shanzhu.entity.ShanzhuSubGoal;
import com.shanzhu.entity.ShanzhuTagRelation;
import com.shanzhu.entity.ShanzhuTask;
import com.shanzhu.mapper.ShanzhuCategoryMapper;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.mapper.ShanzhuGoalProgressMapper;
import com.shanzhu.mapper.ShanzhuSubGoalMapper;
import com.shanzhu.mapper.ShanzhuTagRelationMapper;
import com.shanzhu.mapper.ShanzhuTaskMapper;
import com.shanzhu.model.dto.ShanzhuGoalQueryDTO;
import com.shanzhu.model.dto.ShanzhuGoalSaveDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationQueryDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationSaveDTO;
import com.shanzhu.model.dto.ShanzhuTaskQueryDTO;
import com.shanzhu.model.vo.ShanzhuGoalVO;
import com.shanzhu.model.vo.ShanzhuSubGoalVO;
import com.shanzhu.model.vo.ShanzhuTaskVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuGoalProgressService;
import com.shanzhu.service.ShanzhuGoalService;
import com.shanzhu.service.ShanzhuSubGoalService;
import com.shanzhu.service.ShanzhuTagRelationService;
import com.shanzhu.service.ShanzhuTaskService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ShanzhuGoalServiceImpl extends ServiceImpl<ShanzhuGoalMapper, ShanzhuGoal> implements ShanzhuGoalService {

    private static final String DEFAULT_GOAL_STATUS = "not_started";
    private static final String DEFAULT_GOAL_TYPE = "normal";
    private static final String DEFAULT_PROGRESS_MODE = "manual";
    private static final String COMPLETED_STATUS = "completed";
    private static final String DELETED_FLAG = "1";
    private static final String NORMAL_FLAG = "0";
    private static final String TAG_BIZ_TYPE = "goal";

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Resource
    private ShanzhuSubGoalMapper shanzhuSubGoalMapper;

    @Resource
    private ShanzhuTaskMapper shanzhuTaskMapper;

    @Resource
    private ShanzhuGoalProgressMapper shanzhuGoalProgressMapper;

    @Resource
    private ShanzhuTagRelationMapper shanzhuTagRelationMapper;

    @Resource
    private ShanzhuCategoryMapper shanzhuCategoryMapper;

    @Resource
    private ShanzhuTagRelationService shanzhuTagRelationService;

    @Resource
    private ShanzhuSubGoalService shanzhuSubGoalService;

    @Resource
    private ShanzhuTaskService shanzhuTaskService;

    @Resource
    private ShanzhuGoalProgressService shanzhuGoalProgressService;

    @Override
    public IPage<ShanzhuGoalVO> queryPage(ShanzhuGoalQueryDTO queryDTO) {
        IPage<ShanzhuGoal> goalPage = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        QueryWrapper<ShanzhuGoal> queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.lambda().orderByDesc(ShanzhuGoal::getCreateTime);
        IPage<ShanzhuGoal> pageResult = shanzhuGoalMapper.selectPage(goalPage, queryWrapper);

        IPage<ShanzhuGoalVO> goalVOPage = new Page<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal());
        goalVOPage.setRecords(convertToVOList(pageResult.getRecords()));
        return goalVOPage;
    }

    @Override
    public ShanzhuGoalVO queryById(String id) {
        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoal::getId, id)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
        ShanzhuGoal goal = shanzhuGoalMapper.selectOne(queryWrapper);
        if (goal == null) {
            return null;
        }
        ShanzhuGoalVO goalVO = convertToVO(goal, queryCategoryMap(List.of(goal)));
        fillGoalDetailAggregation(goalVO);
        return goalVO;
    }

    @Override
    public String saveGoal(ShanzhuGoalSaveDTO saveDTO) {
        ShanzhuGoal oldGoal = queryCurrentUserGoal(saveDTO.getId());
        ShanzhuGoal goal = new ShanzhuGoal();
        BeanUtils.copyProperties(saveDTO, goal);
        goal.setUserId(LoginUserContext.getUserId());
        goal.setStatus(StringUtils.hasText(saveDTO.getStatus()) ? saveDTO.getStatus() : DEFAULT_GOAL_STATUS);
        goal.setGoalType(StringUtils.hasText(saveDTO.getGoalType()) ? saveDTO.getGoalType() : DEFAULT_GOAL_TYPE);
        goal.setProgressMode(StringUtils.hasText(saveDTO.getProgressMode()) ? saveDTO.getProgressMode() : DEFAULT_PROGRESS_MODE);
        goal.setProgress(saveDTO.getProgress() == null ? 0 : saveDTO.getProgress());
        goal.setPriority(saveDTO.getPriority() == null ? 2 : saveDTO.getPriority());

        if (COMPLETED_STATUS.equals(goal.getStatus()) && goal.getCompletedTime() == null) {
            goal.setCompletedTime(LocalDateTime.now());
            goal.setProgress(100);
        }

        if (StringUtils.hasText(goal.getId())) {
            if (oldGoal == null) {
                return goal.getId();
            }
            UpdateWrapper<ShanzhuGoal> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .eq(ShanzhuGoal::getId, goal.getId())
                    .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                    .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
            shanzhuGoalMapper.update(goal, updateWrapper);
        } else {
            shanzhuGoalMapper.insert(goal);
        }

        recordGoalProgressChange(oldGoal, goal);

        ShanzhuTagRelationSaveDTO relationSaveDTO = new ShanzhuTagRelationSaveDTO();
        relationSaveDTO.setBizType(TAG_BIZ_TYPE);
        relationSaveDTO.setBizId(goal.getId());
        relationSaveDTO.setTagIds(saveDTO.getTagIds());
        shanzhuTagRelationService.saveRelations(relationSaveDTO);
        return goal.getId();
    }

    @Override
    public void deleteGoal(String id) {
        ShanzhuGoal goal = new ShanzhuGoal();
        goal.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuGoal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuGoal::getId, id)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId());
        shanzhuGoalMapper.update(goal, updateWrapper);

        logicDeleteSubGoals(id);
        logicDeleteTasks(id);
        logicDeleteGoalProgress(id);
        logicDeleteGoalTagRelations(id);
    }

    private void logicDeleteSubGoals(String goalId) {
        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuSubGoal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuSubGoal::getGoalId, goalId)
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId());
        shanzhuSubGoalMapper.update(subGoal, updateWrapper);
    }

    private void logicDeleteTasks(String goalId) {
        ShanzhuTask task = new ShanzhuTask();
        task.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuTask> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuTask::getGoalId, goalId)
                .eq(ShanzhuTask::getUserId, LoginUserContext.getUserId());
        shanzhuTaskMapper.update(task, updateWrapper);
    }

    private void logicDeleteGoalProgress(String goalId) {
        ShanzhuGoalProgress progress = new ShanzhuGoalProgress();
        progress.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuGoalProgress> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuGoalProgress::getGoalId, goalId)
                .eq(ShanzhuGoalProgress::getUserId, LoginUserContext.getUserId());
        shanzhuGoalProgressMapper.update(progress, updateWrapper);
    }

    private void logicDeleteGoalTagRelations(String goalId) {
        ShanzhuTagRelation relation = new ShanzhuTagRelation();
        relation.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuTagRelation> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuTagRelation::getBizType, TAG_BIZ_TYPE)
                .eq(ShanzhuTagRelation::getBizId, goalId)
                .eq(ShanzhuTagRelation::getUserId, LoginUserContext.getUserId());
        shanzhuTagRelationMapper.update(relation, updateWrapper);
    }

    private ShanzhuGoal queryCurrentUserGoal(String goalId) {
        if (!StringUtils.hasText(goalId)) {
            return null;
        }

        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoal::getId, goalId)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
        return shanzhuGoalMapper.selectOne(queryWrapper);
    }

    private void recordGoalProgressChange(ShanzhuGoal oldGoal, ShanzhuGoal goal) {
        if (oldGoal == null || Objects.equals(oldGoal.getProgress(), goal.getProgress())) {
            return;
        }

        String title = COMPLETED_STATUS.equals(goal.getStatus()) ? "完成目标" : "更新目标进度";
        String content = String.format("目标「%s」进度由 %s%% 更新为 %s%%", goal.getTitle(), oldGoal.getProgress(), goal.getProgress());
        shanzhuGoalProgressService.recordProgress(goal.getId(), null, null, title, content, oldGoal.getProgress(), goal.getProgress());
    }

    private QueryWrapper<ShanzhuGoal> buildQueryWrapper(ShanzhuGoalQueryDTO queryDTO) {
        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.lambda().like(ShanzhuGoal::getTitle, queryDTO.getKeyword());
        }
        if (StringUtils.hasText(queryDTO.getCategoryId())) {
            queryWrapper.lambda().eq(ShanzhuGoal::getCategoryId, queryDTO.getCategoryId());
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            queryWrapper.lambda().eq(ShanzhuGoal::getStatus, queryDTO.getStatus());
        }
        if (queryDTO.getStartDate() != null) {
            queryWrapper.lambda().ge(ShanzhuGoal::getStartDate, queryDTO.getStartDate());
        }
        if (queryDTO.getDeadline() != null) {
            queryWrapper.lambda().le(ShanzhuGoal::getDeadline, queryDTO.getDeadline());
        }
        applyTagFilter(queryWrapper, queryDTO);
        return queryWrapper;
    }

    private void applyTagFilter(QueryWrapper<ShanzhuGoal> queryWrapper, ShanzhuGoalQueryDTO queryDTO) {
        if (queryDTO.getTagIds() == null || queryDTO.getTagIds().isEmpty()) {
            return;
        }

        QueryWrapper<ShanzhuTagRelation> relationQueryWrapper = new QueryWrapper<>();
        relationQueryWrapper.lambda()
                .select(ShanzhuTagRelation::getBizId)
                .eq(ShanzhuTagRelation::getBizType, TAG_BIZ_TYPE)
                .eq(ShanzhuTagRelation::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTagRelation::getDelFlag, NORMAL_FLAG)
                .in(ShanzhuTagRelation::getTagId, queryDTO.getTagIds());
        List<String> goalIds = shanzhuTagRelationMapper.selectList(relationQueryWrapper).stream()
                .map(ShanzhuTagRelation::getBizId)
                .distinct()
                .toList();

        if (goalIds.isEmpty()) {
            queryWrapper.lambda().eq(ShanzhuGoal::getId, "");
            return;
        }
        queryWrapper.lambda().in(ShanzhuGoal::getId, goalIds);
    }

    private List<ShanzhuGoalVO> convertToVOList(List<ShanzhuGoal> goals) {
        Map<String, ShanzhuCategory> categoryMap = queryCategoryMap(goals);
        return goals.stream()
                .map(goal -> convertToVO(goal, categoryMap))
                .toList();
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

        return shanzhuCategoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(ShanzhuCategory::getId, category -> category));
    }

    private ShanzhuGoalVO convertToVO(ShanzhuGoal goal, Map<String, ShanzhuCategory> categoryMap) {
        ShanzhuGoalVO goalVO = new ShanzhuGoalVO();
        BeanUtils.copyProperties(goal, goalVO);

        ShanzhuCategory category = categoryMap.get(goal.getCategoryId());
        if (category != null) {
            goalVO.setCategoryName(category.getName());
            goalVO.setCategoryColor(category.getColor());
        }

        ShanzhuTagRelationQueryDTO relationQueryDTO = new ShanzhuTagRelationQueryDTO();
        relationQueryDTO.setBizType(TAG_BIZ_TYPE);
        relationQueryDTO.setBizId(goal.getId());
        goalVO.setTags(shanzhuTagRelationService.queryRelations(relationQueryDTO));
        return goalVO;
    }

    private void fillGoalDetailAggregation(ShanzhuGoalVO goalVO) {
        List<ShanzhuSubGoalVO> subGoals = shanzhuSubGoalService.queryByGoalId(goalVO.getId());

        ShanzhuTaskQueryDTO taskQueryDTO = new ShanzhuTaskQueryDTO();
        taskQueryDTO.setGoalId(goalVO.getId());
        List<ShanzhuTaskVO> tasks = shanzhuTaskService.queryTaskList(taskQueryDTO);

        Map<String, List<ShanzhuTaskVO>> subGoalIdToTasks = tasks.stream()
                .filter(task -> StringUtils.hasText(task.getSubGoalId()))
                .collect(Collectors.groupingBy(ShanzhuTaskVO::getSubGoalId));

        subGoals.forEach(subGoal -> subGoal.setTasks(
                subGoalIdToTasks.getOrDefault(subGoal.getId(), List.of())
        ));

        List<ShanzhuTaskVO> unassignedTasks = tasks.stream()
                .filter(task -> !StringUtils.hasText(task.getSubGoalId()))
                .toList();

        long completedTaskCount = tasks.stream()
                .filter(task -> "completed".equals(task.getStatus()))
                .count();

        goalVO.setSubGoals(subGoals);
        goalVO.setUnassignedTasks(unassignedTasks);
        goalVO.setSubGoalCount(subGoals.size());
        goalVO.setTotalTaskCount(tasks.size());
        goalVO.setCompletedTaskCount(Math.toIntExact(completedTaskCount));
    }
}
