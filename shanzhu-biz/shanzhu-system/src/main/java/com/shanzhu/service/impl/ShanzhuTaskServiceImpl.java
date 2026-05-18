package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuTask;
import com.shanzhu.mapper.ShanzhuTaskMapper;
import com.shanzhu.model.dto.ShanzhuTaskQueryDTO;
import com.shanzhu.model.dto.ShanzhuTaskSaveDTO;
import com.shanzhu.model.dto.ShanzhuTaskSortDTO;
import com.shanzhu.model.dto.ShanzhuTaskStatusDTO;
import com.shanzhu.model.vo.ShanzhuTaskVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuTaskService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShanzhuTaskServiceImpl extends ServiceImpl<ShanzhuTaskMapper, ShanzhuTask> implements ShanzhuTaskService {

    private static final String DEFAULT_STATUS = "not_started";
    private static final String DELETED_FLAG = "1";
    private static final String NORMAL_FLAG = "0";
    private static final String COMPLETED_STATUS = "completed";

    @Override
    public List<ShanzhuTaskVO> queryTaskList(ShanzhuTaskQueryDTO queryDTO) {
        QueryWrapper<ShanzhuTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTask::getGoalId, queryDTO.getGoalId())
                .eq(ShanzhuTask::getUserId, LoginUserContext.getUserId())
                .eq(ShanzhuTask::getDelFlag, NORMAL_FLAG)
                .eq(StringUtils.hasText(queryDTO.getSubGoalId()), ShanzhuTask::getSubGoalId, queryDTO.getSubGoalId())
                .eq(StringUtils.hasText(queryDTO.getStatus()), ShanzhuTask::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getPlannedDate() != null, ShanzhuTask::getPlannedDate, queryDTO.getPlannedDate())
                .orderByAsc(ShanzhuTask::getSortOrder)
                .orderByAsc(ShanzhuTask::getPlannedDate)
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

    private List<ShanzhuTaskVO> convertToVOList(List<ShanzhuTask> tasks) {
        List<ShanzhuTaskVO> taskVOList = new ArrayList<>();
        tasks.forEach(task -> {
            ShanzhuTaskVO taskVO = new ShanzhuTaskVO();
            BeanUtils.copyProperties(task, taskVO);
            taskVOList.add(taskVO);
        });
        return taskVOList;
    }
}
