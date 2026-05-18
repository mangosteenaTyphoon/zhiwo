package com.shanzhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanzhu.model.dto.ShanzhuHabitQueryDTO;
import com.shanzhu.model.dto.ShanzhuHabitSaveDTO;
import com.shanzhu.model.vo.ShanzhuHabitStatsVO;
import com.shanzhu.model.vo.ShanzhuHabitTodayVO;
import com.shanzhu.model.vo.ShanzhuHabitVO;

import java.util.List;

public interface ShanzhuHabitService {
    /**
     * 分页查询习惯列表
     */
    IPage<ShanzhuHabitVO> queryHabitPage(ShanzhuHabitQueryDTO queryDTO);

    /**
     * 查询习惯列表
     */
    List<ShanzhuHabitVO> queryHabitList(ShanzhuHabitQueryDTO queryDTO);

    /**
     * 保存习惯
     */
    String saveHabit(ShanzhuHabitSaveDTO saveDTO);

    /**
     * 删除习惯
     */
    void deleteHabit(String id);

    /**
     * 暂停习惯
     */
    void pauseHabit(String id);

    /**
     * 恢复习惯
     */
    void resumeHabit(String id);

    /**
     * 查询今日待打卡列表
     */
    List<ShanzhuHabitTodayVO> queryTodayHabits();

    /**
     * 查询习惯统计
     */
    ShanzhuHabitStatsVO queryHabitStats();
}
