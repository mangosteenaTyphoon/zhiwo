package com.shanzhu.service;

import com.shanzhu.model.dto.ShanzhuTagRelationDeleteDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationQueryDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationSaveDTO;
import com.shanzhu.model.vo.ShanzhuTagVO;

import java.util.List;

public interface ShanzhuTagRelationService {
    /**
     * 保存业务标签关系
     */
    void saveRelations(ShanzhuTagRelationSaveDTO saveDTO);

    /**
     * 查询业务对象绑定的标签
     */
    List<ShanzhuTagVO> queryRelations(ShanzhuTagRelationQueryDTO queryDTO);

    /**
     * 删除业务标签关系
     */
    void deleteRelations(ShanzhuTagRelationDeleteDTO deleteDTO);
}
