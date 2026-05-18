package com.shanzhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanzhu.model.dto.ShanzhuTaskQueryDTO;
import com.shanzhu.model.dto.ShanzhuTaskSaveDTO;
import com.shanzhu.model.dto.ShanzhuTaskSortDTO;
import com.shanzhu.model.dto.ShanzhuTaskStatusDTO;
import com.shanzhu.model.vo.ShanzhuTaskVO;

import java.util.List;

public interface ShanzhuTaskService {
    /**
     * 分页查询任务列表
     */
    IPage<ShanzhuTaskVO> queryTaskPage(ShanzhuTaskQueryDTO queryDTO);

    /**
     * 查询目标下的任务列表
     */
    List<ShanzhuTaskVO> queryTaskList(ShanzhuTaskQueryDTO queryDTO);

    /**
     * 保存任务
     */
    String saveTask(ShanzhuTaskSaveDTO saveDTO);

    /**
     * 删除任务
     */
    void deleteTask(String id);

    /**
     * 更新任务状态
     */
    void updateStatus(ShanzhuTaskStatusDTO statusDTO);

    /**
     * 调整任务排序
     */
    void updateSort(ShanzhuTaskSortDTO sortDTO);
}
