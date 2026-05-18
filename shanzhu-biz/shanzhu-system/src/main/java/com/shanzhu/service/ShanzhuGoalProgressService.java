package com.shanzhu.service;

import com.shanzhu.model.dto.ShanzhuGoalProgressQueryDTO;
import com.shanzhu.model.dto.ShanzhuGoalProgressSaveDTO;
import com.shanzhu.model.vo.ShanzhuGoalProgressVO;

import java.util.List;

public interface ShanzhuGoalProgressService {
    /**
     * 查询目标进展记录
     */
    List<ShanzhuGoalProgressVO> queryByGoalId(ShanzhuGoalProgressQueryDTO queryDTO);

    /**
     * 保存目标进展记录
     */
    String saveProgress(ShanzhuGoalProgressSaveDTO saveDTO);

    /**
     * 删除目标进展记录
     */
    void deleteProgress(String id);

    /**
     * 自动记录目标进展
     */
    void recordProgress(String goalId, String subGoalId, String taskId, String title, String content, Integer progressBefore, Integer progressAfter);
}
