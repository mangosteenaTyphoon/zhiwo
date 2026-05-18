package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuSubGoal;
import com.shanzhu.mapper.ShanzhuSubGoalMapper;
import com.shanzhu.model.dto.ShanzhuSubGoalProgressDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalSaveDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalSortDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalStatusDTO;
import com.shanzhu.model.vo.ShanzhuSubGoalVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuSubGoalService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShanzhuSubGoalServiceImpl extends ServiceImpl<ShanzhuSubGoalMapper, ShanzhuSubGoal> implements ShanzhuSubGoalService {

    private static final String DEFAULT_STATUS = "not_started";
    private static final String DELETED_FLAG = "1";
    private static final String NORMAL_FLAG = "0";
    private static final String COMPLETED_STATUS = "completed";

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
        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        BeanUtils.copyProperties(saveDTO, subGoal);
        subGoal.setUserId(LoginUserContext.getUserId());
        subGoal.setStatus(StringUtils.hasText(saveDTO.getStatus()) ? saveDTO.getStatus() : DEFAULT_STATUS);
        subGoal.setProgress(saveDTO.getProgress() == null ? 0 : saveDTO.getProgress());
        subGoal.setWeight(saveDTO.getWeight() == null ? 1 : saveDTO.getWeight());
        subGoal.setSortOrder(saveDTO.getSortOrder() == null ? 0 : saveDTO.getSortOrder());

        if (COMPLETED_STATUS.equals(subGoal.getStatus()) && subGoal.getCompletedTime() == null) {
            subGoal.setCompletedTime(LocalDateTime.now());
        }

        if (StringUtils.hasText(subGoal.getId())) {
            updateById(subGoal);
        } else {
            save(subGoal);
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
        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setId(statusDTO.getId());
        subGoal.setStatus(statusDTO.getStatus());
        if (COMPLETED_STATUS.equals(statusDTO.getStatus())) {
            subGoal.setCompletedTime(LocalDateTime.now());
            subGoal.setProgress(100);
        }
        updateById(subGoal);
    }

    @Override
    public void updateProgress(ShanzhuSubGoalProgressDTO progressDTO) {
        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setId(progressDTO.getId());
        subGoal.setProgress(progressDTO.getProgress());
        updateById(subGoal);
    }

    @Override
    public void updateSort(ShanzhuSubGoalSortDTO sortDTO) {
        ShanzhuSubGoal subGoal = new ShanzhuSubGoal();
        subGoal.setId(sortDTO.getId());
        subGoal.setSortOrder(sortDTO.getSortOrder());
        updateById(subGoal);
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
