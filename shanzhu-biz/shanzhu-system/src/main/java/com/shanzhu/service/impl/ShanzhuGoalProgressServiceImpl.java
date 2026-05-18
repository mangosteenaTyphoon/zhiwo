package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.entity.ShanzhuGoalProgress;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.mapper.ShanzhuGoalProgressMapper;
import com.shanzhu.model.dto.ShanzhuGoalProgressQueryDTO;
import com.shanzhu.model.dto.ShanzhuGoalProgressSaveDTO;
import com.shanzhu.model.vo.ShanzhuGoalProgressVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuGoalProgressService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShanzhuGoalProgressServiceImpl extends ServiceImpl<ShanzhuGoalProgressMapper, ShanzhuGoalProgress> implements ShanzhuGoalProgressService {
    private static final String DELETED_FLAG = "1";
    private static final String NORMAL_FLAG = "0";

    @Resource
    private ShanzhuGoalMapper shanzhuGoalMapper;

    @Override
    public List<ShanzhuGoalProgressVO> queryByGoalId(ShanzhuGoalProgressQueryDTO queryDTO) {
        QueryWrapper<ShanzhuGoalProgress> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuGoalProgress::getGoalId, queryDTO.getGoalId())
                .eq(ShanzhuGoalProgress::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoalProgress::getDelFlag, NORMAL_FLAG)
                .orderByDesc(ShanzhuGoalProgress::getRecordDate)
                .orderByDesc(ShanzhuGoalProgress::getCreateTime);
        return convertToVOList(list(queryWrapper));
    }

    @Override
    public String saveProgress(ShanzhuGoalProgressSaveDTO saveDTO) {
        if (!isCurrentUserGoal(saveDTO.getGoalId())) {
            return null;
        }

        ShanzhuGoalProgress progress = new ShanzhuGoalProgress();
        BeanUtils.copyProperties(saveDTO, progress);
        progress.setUserId(LoginUserContext.getUserId());
        progress.setRecordDate(saveDTO.getRecordDate() == null ? LocalDate.now() : saveDTO.getRecordDate());
        save(progress);
        return progress.getId();
    }

    @Override
    public void deleteProgress(String id) {
        ShanzhuGoalProgress progress = new ShanzhuGoalProgress();
        progress.setDelFlag(DELETED_FLAG);
        UpdateWrapper<ShanzhuGoalProgress> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(ShanzhuGoalProgress::getId, id)
                .eq(ShanzhuGoalProgress::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuGoalProgress::getDelFlag, NORMAL_FLAG);
        update(progress, updateWrapper);
    }

    @Override
    public void recordProgress(String goalId, String subGoalId, String taskId, String title, String content, Integer progressBefore, Integer progressAfter) {
        if (!StringUtils.hasText(goalId) || !isCurrentUserGoal(goalId)) {
            return;
        }

        ShanzhuGoalProgress progress = new ShanzhuGoalProgress();
        progress.setGoalId(goalId);
        progress.setSubGoalId(subGoalId);
        progress.setTaskId(taskId);
        progress.setUserId(LoginUserContext.getUserId());
        progress.setTitle(title);
        progress.setContent(content);
        progress.setProgressBefore(progressBefore);
        progress.setProgressAfter(progressAfter);
        progress.setRecordDate(LocalDate.now());
        save(progress);
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

    private List<ShanzhuGoalProgressVO> convertToVOList(List<ShanzhuGoalProgress> progressList) {
        List<ShanzhuGoalProgressVO> progressVOList = new ArrayList<>();
        progressList.forEach(progress -> {
            ShanzhuGoalProgressVO progressVO = new ShanzhuGoalProgressVO();
            BeanUtils.copyProperties(progress, progressVO);
            progressVOList.add(progressVO);
        });
        return progressVOList;
    }
}
