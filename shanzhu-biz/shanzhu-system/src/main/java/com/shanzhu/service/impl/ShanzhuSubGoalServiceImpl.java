package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
            updateById(subGoal);
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
        subGoal.setId(id);
        subGoal.setDelFlag(DELETED_FLAG);
        updateById(subGoal);
    }

    @Override
    public void updateStatus(ShanzhuSubGoalStatusDTO statusDTO) {
        ShanzhuSubGoal oldSubGoal = queryCurrentUserSubGoal(statusDTO.getId());
        if (oldSubGoal == null) {
            return;
        }

        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setId(statusDTO.getId());
        subGoal.setStatus(statusDTO.getStatus());
        if (COMPLETED_STATUS.equals(statusDTO.getStatus())) {
            subGoal.setCompletedTime(LocalDateTime.now());
            subGoal.setProgress(100);
        }
        updateById(subGoal);

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
        subGoal.setId(progressDTO.getId());
        subGoal.setProgress(progressDTO.getProgress());
        updateById(subGoal);

        recordSubGoalProgressChange(oldSubGoal, progressDTO.getProgress(), "更新子目标进度");
        refreshGoalProgress(oldSubGoal.getGoalId());
    }

    @Override
    public void updateSort(ShanzhuSubGoalSortDTO sortDTO) {
        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setId(sortDTO.getId());
        subGoal.setSortOrder(sortDTO.getSortOrder());
        updateById(subGoal);
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
        ShanzhuGoal goal = shanzhuGoalMapper.selectById(goalId);
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
        updateGoal.setId(goalId);
        updateGoal.setProgress(progressAfter);
        shanzhuGoalMapper.updateById(updateGoal);
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
