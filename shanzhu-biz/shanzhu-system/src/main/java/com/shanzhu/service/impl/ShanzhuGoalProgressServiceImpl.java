package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuGoalProgress;
import com.shanzhu.mapper.ShanzhuGoalProgressMapper;
import com.shanzhu.model.dto.ShanzhuGoalProgressQueryDTO;
import com.shanzhu.model.dto.ShanzhuGoalProgressSaveDTO;
import com.shanzhu.model.vo.ShanzhuGoalProgressVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuGoalProgressService;
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
        progress.setId(id);
        progress.setDelFlag(DELETED_FLAG);
        updateById(progress);
    }

    @Override
    public void recordProgress(String goalId, String subGoalId, String taskId, String title, String content, Integer progressBefore, Integer progressAfter) {
        if (!StringUtils.hasText(goalId)) {
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
