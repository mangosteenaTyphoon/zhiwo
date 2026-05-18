package com.shanzhu.service;

import com.shanzhu.model.dto.ShanzhuCategorySaveDTO;
import com.shanzhu.model.vo.ShanzhuCategoryVO;

import java.util.List;

public interface ShanzhuCategoryService {
    /**
     * 查询启用的分类列表
     */
    List<ShanzhuCategoryVO> queryList();

    /**
     * 新增当前用户分类
     */
    String addCategory(ShanzhuCategorySaveDTO saveDTO);
}
