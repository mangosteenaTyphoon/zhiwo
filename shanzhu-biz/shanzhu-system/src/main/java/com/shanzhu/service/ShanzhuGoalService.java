package com.shanzhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanzhu.model.dto.ShanzhuGoalQueryDTO;
import com.shanzhu.model.dto.ShanzhuGoalSaveDTO;
import com.shanzhu.model.vo.ShanzhuGoalVO;

public interface ShanzhuGoalService {
    /**
     * 分页查询目标列表
     */
    IPage<ShanzhuGoalVO> queryPage(ShanzhuGoalQueryDTO queryDTO);

    /**
     * 根据ID查询目标详情
     */
    ShanzhuGoalVO queryById(String id);

    /**
     * 保存目标
     */
    String saveGoal(ShanzhuGoalSaveDTO saveDTO);

    /**
     * 删除目标
     */
    void deleteGoal(String id);
}
