package com.shanzhu.service;

import com.shanzhu.model.dto.ShanzhuHabitCheckinQueryDTO;
import com.shanzhu.model.dto.ShanzhuHabitCheckinSaveDTO;
import com.shanzhu.model.vo.ShanzhuHabitCheckinVO;

import java.util.List;

public interface ShanzhuHabitCheckinService {
    /**
     * 打卡或修改打卡
     */
    String saveCheckin(ShanzhuHabitCheckinSaveDTO saveDTO);

    /**
     * 取消打卡
     */
    void cancelCheckin(String id);

    /**
     * 查询打卡记录
     */
    List<ShanzhuHabitCheckinVO> queryCheckins(ShanzhuHabitCheckinQueryDTO queryDTO);
}
