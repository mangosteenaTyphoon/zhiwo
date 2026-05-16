package com.shanzhu.service;

import com.shanzhu.model.dto.ShanzhuTagQueryDTO;
import com.shanzhu.model.dto.ShanzhuTagSaveDTO;
import com.shanzhu.model.vo.ShanzhuTagVO;

import java.util.List;

public interface ShanzhuTagService {
    /**
     * 查询标签列表
     */
    List<ShanzhuTagVO> queryList(ShanzhuTagQueryDTO queryDTO);

    /**
     * 搜索标签
     */
    List<ShanzhuTagVO> search(ShanzhuTagQueryDTO queryDTO);

    /**
     * 新增标签
     */
    String add(ShanzhuTagSaveDTO saveDTO);
}
