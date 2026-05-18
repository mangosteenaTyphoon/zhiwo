package com.shanzhu.service;

import com.shanzhu.model.dto.ShanzhuSubGoalProgressDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalSaveDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalSortDTO;
import com.shanzhu.model.dto.ShanzhuSubGoalStatusDTO;
import com.shanzhu.model.vo.ShanzhuSubGoalVO;

import java.util.List;

public interface ShanzhuSubGoalService {
    /**
     * 查询目标下的子目标列表
     */
    List<ShanzhuSubGoalVO> queryByGoalId(String goalId);

    /**
     * 保存子目标
     */
    String saveSubGoal(ShanzhuSubGoalSaveDTO saveDTO);

    /**
     * 删除子目标
     */
    void deleteSubGoal(String id);

    /**
     * 更新子目标状态
     */
    void updateStatus(ShanzhuSubGoalStatusDTO statusDTO);

    /**
     * 更新子目标进度
     */
    void updateProgress(ShanzhuSubGoalProgressDTO progressDTO);

    /**
     * 调整子目标排序
     */
    void updateSort(ShanzhuSubGoalSortDTO sortDTO);
}
