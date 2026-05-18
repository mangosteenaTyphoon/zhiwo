package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.entity.ShanzhuSubGoal;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.mapper.ShanzhuSubGoalMapper;
import com.shanzhu.model.dto.ShanzhuSubGoalProgressDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalSaveDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalSortDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalStatusDTO;
import com.shanzhu.model.vo.ShanzhuSubGoalVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuGoalProgressService;
import com.shanzhu.service.ShanzhuSubGoalService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ShanzhuSubGoalServiceImpl extends ServiceImpl<ShanzhuSubGoalMapper, ShanzhuSubGoal> implements ShanzhuSubGoalService {

    private static final String DEFAULT_STATUS = "not_started";
    private static final String DELETED_FLAG = "1";
    private static final String NORMAL_FLAG = "0";
    private static final String COMPLETED_STATUS = "completed";

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Resource
    private ShanzhuGoalProgressService shanzhuGoalProgressService;

    @Override
    public List<ShanzhuSubGoalVO> queryByGoalId(String goalId) {
        QueryWrapper<ShanzhuSubGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuSubGoal::getGoalId, goalId)
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG)
                .orderByAsc(ShanzhuSubGoal::getSortOrder)
                .orderByDesc(ShanzhuSubGoal::getCreateTime);
        return convertToVOList(list(queryWrapper));
    }

    @Override
    public String saveSubGoal(ShanzhuSubGoalSaveDTO saveDTO) {
        ShanzhuSubGoal oldSubGoal = queryCurrentUserSubGoal(saveDTO.getId());
        if (StringUtils.hasText(saveDTO.getId()) && oldSubGoal == null) {
            return saveDTO.getId();
        }
        if (!isCurrentUserGoal(saveDTO.getGoalId())) {
            return saveDTO.getId();
        }

        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        BeanUtils.copyProperties(saveDTO, subGoal);
        subGoal.setUserId(LoginUserContext.getUserId());
        subGoal.setStatus(StringUtils.hasText(saveDTO.getStatus()) ? saveDTO.getStatus() : DEFAULT_STATUS);
        subGoal.setProgress(saveDTO.getProgress() == null ? 0 : saveDTO.getProgress());
        subGoal.setWeight(saveDTO.getWeight() == null ? 1 : saveDTO.getWeight());
        subGoal.setSortOrder(saveDTO.getSortOrder() == null ? 0 : saveDTO.getSortOrder());

        if (COMPLETED_STATUS.equals(subGoal.getStatus()) && subGoal.getCompletedTime() == null) {
            subGoal.setCompletedTime(LocalDateTime.now());
            subGoal.setProgress(100);
        }

        if (StringUtils.hasText(subGoal.getId())) {
            UpdateWrapper<ShanzhuSubGoal> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .eq(ShanzhuSubGoal::getId, subGoal.getId())
                    .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                    .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG);
            update(subGoal, updateWrapper);
        } else {
            save(subGoal);
        }

        if (oldSubGoal != null) {
            String progressTitle = COMPLETED_STATUS.equals(subGoal.getStatus()) ? "完成子目标" : "更新子目标进度";
            recordSubGoalProgressChange(oldSubGoal, subGoal.getProgress(), progressTitle);
            refreshGoalProgress(oldSubGoal.getGoalId());
        }
        return subGoal.getId();
    }

    @Override
    public void deleteSubGoal(String id) {
        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuSubGoal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuSubGoal::getId, id)
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG);
        update(subGoal, updateWrapper);
    }

    @Override
    public void updateStatus(ShanzhuSubGoalStatusDTO statusDTO) {
        ShanzhuSubGoal oldSubGoal = queryCurrentUserSubGoal(statusDTO.getId());
        if (oldSubGoal == null) {
            return;
        }

        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setStatus(statusDTO.getStatus());
        if (COMPLETED_STATUS.equals(statusDTO.getStatus())) {
            subGoal.setCompletedTime(LocalDateTime.now());
            subGoal.setProgress(100);
        }
        UpdateWrapper<ShanzhuSubGoal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuSubGoal::getId, statusDTO.getId())
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG);
        update(subGoal, updateWrapper);

        if (COMPLETED_STATUS.equals(statusDTO.getStatus())) {
            recordSubGoalProgressChange(oldSubGoal, 100, "完成子目标");
        }
        refreshGoalProgress(oldSubGoal.getGoalId());
    }

    @Override
    public void updateProgress(ShanzhuSubGoalProgressDTO progressDTO) {
        ShanzhuSubGoal oldSubGoal = queryCurrentUserSubGoal(progressDTO.getId());
        if (oldSubGoal == null) {
            return;
        }

        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setProgress(progressDTO.getProgress());
        UpdateWrapper<ShanzhuSubGoal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuSubGoal::getId, progressDTO.getId())
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG);
        update(subGoal, updateWrapper);

        recordSubGoalProgressChange(oldSubGoal, progressDTO.getProgress(), "更新子目标进度");
        refreshGoalProgress(oldSubGoal.getGoalId());
    }

    @Override
    public void updateSort(ShanzhuSubGoalSortDTO sortDTO) {
        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setSortOrder(sortDTO.getSortOrder());
        UpdateWrapper<ShanzhuSubGoal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuSubGoal::getId, sortDTO.getId())
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG);
        update(subGoal, updateWrapper);
    }

    private boolean isCurrentUserGoal(String goalId) {
        if (!StringUtils.hasText(goalId)) {
            return false;
        }

        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoal::getId, goalId)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
        return shanzhuGoalMapper.selectCount(queryWrapper) > 0;
    }

    private ShanzhuSubGoal queryCurrentUserSubGoal(String subGoalId) {
        QueryWrapper<ShanzhuSubGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuSubGoal::getId, subGoalId)
                .eq(ShanzhuSubGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuSubGoal::getDelFlag, NORMAL_FLAG);
        return getOne(queryWrapper);
    }

    private void recordSubGoalProgressChange(ShanzhuSubGoal oldSubGoal, Integer progressAfter, String title) {
        if (Objects.equals(oldSubGoal.getProgress(), progressAfter)) {
            return;
        }

        String content = String.format("子目标「%s」进度由 %s%% 更新为 %s%%", oldSubGoal.getTitle(), oldSubGoal.getProgress(), progressAfter);
        shanzhuGoalProgressService.recordProgress(
                oldSubGoal.getGoalId(),
                oldSubGoal.getId(),
                null,
                title,
                content,
                oldSubGoal.getProgress(),
                progressAfter
        );
    }

    private void refreshGoalProgress(String goalId) {
        QueryWrapper<ShanzhuGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoal::getId, goalId)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
        ShanzhuGoal goal = shanzhuGoalMapper.selectOne(queryWrapper);
        if (goal == null) {
            return;
        }

        List<ShanzhuSubGoalVO> subGoals = queryByGoalId(goalId);
        if (subGoals.isEmpty()) {
            return;
        }

        int totalWeight = subGoals.stream()
                .mapToInt(subGoal -> subGoal.getWeight() == null ? 1 : subGoal.getWeight())
                .sum();
        int progressSum = subGoals.stream()
                .mapToInt(subGoal -> (subGoal.getProgress() == null ? 0 : subGoal.getProgress()) * (subGoal.getWeight() == null ? 1 : subGoal.getWeight()))
                .sum();
        int progressAfter = totalWeight == 0 ? 0 : Math.round((float) progressSum / totalWeight);
        if (Objects.equals(goal.getProgress(), progressAfter)) {
            return;
        }

        ShanzhuGoal updateGoal = new ShanzhuGoal();
        updateGoal.setProgress(progressAfter);
        UpdateWrapper<ShanzhuGoal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuGoal::getId, goalId)
                .eq(ShanzhuGoal::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoal::getDelFlag, NORMAL_FLAG);
        shanzhuGoalMapper.update(updateGoal, updateWrapper);
        String content = String.format("目标「%s」进度由 %s%% 汇总更新为 %s%%", goal.getTitle(), goal.getProgress(), progressAfter);
        shanzhuGoalProgressService.recordProgress(goalId, null, null, "汇总目标进度", content, goal.getProgress(), progressAfter);
    }

    private List<ShanzhuSubGoalVO> convertToVOList(List<ShanzhuSubGoal> subGoals) {
        List<ShanzhuSubGoalVO> subGoalVOList = new ArrayList<>();
        subGoals.forEach(subGoal -> {
            ShanzhuSubGoalVO subGoalVO = new ShanzhuSubGoalVO();
            BeanUtils.copyProperties(subGoal, subGoalVO);
            subGoalVOList.add(subGoalVO);
        });
        return subGoalVOList;
    }
}
