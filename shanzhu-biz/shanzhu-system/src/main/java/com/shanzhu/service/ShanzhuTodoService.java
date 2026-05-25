package com.shanzhu.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shanzhu.model.dto.ShanzhuTodoQueryDTO;
import com.shanzhu.model.dto.ShanzhuTodoSaveDTO;
import com.shanzhu.model.vo.ShanzhuTodoVO;

import java.util.List;

public interface ShanzhuTodoService {
    /**
     * 分页查询Todo列表
     */
    IPage<ShanzhuTodoVO> queryTodoPage(ShanzhuTodoQueryDTO queryDTO);

    /**
     * 查询Todo列表
     */
    List<ShanzhuTodoVO> queryTodoList(ShanzhuTodoQueryDTO queryDTO);

    /**
     * 保存Todo
     */
    String saveTodo(ShanzhuTodoSaveDTO saveDTO);

    /**
     * 删除Todo
     */
    void deleteTodo(String id);

    /**
     * 完成Todo
     */
    void completeTodo(String id);

    /**
     * 取消完成Todo
     */
    void uncompleteTodo(String id);

    /**
     * 将Todo移入今日待办
     */
    void moveToToday(String id);

    /**
     * 将Todo移回收集箱
     */
    void moveToInbox(String id);

    /**
     * 归档Todo
     */
    void archiveTodo(String id);

    /**
     * 将Todo转为任务
     */
    String convertToTask(String id);
}
