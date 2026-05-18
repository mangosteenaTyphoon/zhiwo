package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuCategory;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.mapper.ShanzhuCategoryMapper;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.model.dto.ShanzhuGoalQueryDTO;
import com.shanzhu.model.dto.ShanzhuGoalSaveDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationQueryDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationSaveDTO;
import com.shanzhu.model.dto.ShanzhuTaskQueryDTO;
import com.shanzhu.model.vo.ShanzhuGoalVO;
import com.shanzhu.model.vo.ShanzhuSubGoalVO;
import com.shanzhu.model.vo.ShanzhuTaskVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuGoalService;
import com.shanzhu.service.ShanzhuSubGoalService;
import com.shanzhu.service.ShanzhuTagRelationService;
import com.shanzhu.service.ShanzhuTaskService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ShanzhuGoalServiceImpl extends ServiceImpl<ShanzhuGoalMapper, ShanzhuGoal> implements ShanzhuGoalService {

    private static final String DEFAULT_GOAL_STATUS = "not_started";
    private static final String DEFAULT_GOAL_TYPE = "normal";
    private static final String DEFAULT_PROGRESS_MODE = "manual";
    private static final String TAG_BIZ_TYPE = "goal";

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Resource
    private ShanzhuCategoryMapper shanzhuCategoryMapper;

    @Resource
    private ShanzhuTagRelationService shanzhuTagRelationService;

    @Resource
    private ShanzhuSubGoalService shanzhuSubGoalService;

    @Resource
    private ShanzhuTaskService shanzhuTaskService;

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
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId());
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
        ShanzhuGoal goal = new ShanzhuGoal();
        BeanUtils.copyProperties(saveDTO, goal);
        goal.setUserId(LoginUserContext.getUserId());
        goal.setStatus(StringUtils.hasText(saveDTO.getStatus()) ? saveDTO.getStatus() : DEFAULT_GOAL_STATUS);
        goal.setGoalType(StringUtils.hasText(saveDTO.getGoalType()) ? saveDTO.getGoalType() : DEFAULT_GOAL_TYPE);
        goal.setProgressMode(StringUtils.hasText(saveDTO.getProgressMode()) ? saveDTO.getProgressMode() : DEFAULT_PROGRESS_MODE);
        goal.setProgress(saveDTO.getProgress() == null ? 0 : saveDTO.getProgress());
        goal.setPriority(saveDTO.getPriority() == null ? 2 : saveDTO.getPriority());

        if (StringUtils.hasText(goal.getId())) {
            shanzhuGoalMapper.updateById(goal);
        } else {
            shanzhuGoalMapper.insert(goal);
        }

        ShanzhuTagRelationSaveDTO relationSaveDTO = new ShanzhuTagRelationSaveDTO();
        relationSaveDTO.setBizType(TAG_BIZ_TYPE);
        relationSaveDTO.setBizId(goal.getId());
        relationSaveDTO.setTagIds(saveDTO.getTagIds());
        shanzhuTagRelationService.saveRelations(relationSaveDTO);
        return goal.getId();
    }

    private QueryWrapper<ShanzhuGoal> buildQueryWrapper(ShanzhuGoalQueryDTO queryDTO) {
        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId());

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
        return queryWrapper;
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
