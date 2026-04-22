package com.shanzhu.service;

import com.shanzhu.entity.SysViewTab;
import com.shanzhu.security.model.CurrentRouter;
import com.shanzhu.security.model.CurrentViewTab;

import java.util.List;

public interface SysViewTabService {
    /**
     * 根据用户id查询viewTab数据
     */
    List<CurrentViewTab> selectByUserId(String userId, List<CurrentRouter> routerVOList);

    /**
     * 保存viewTab收藏数据
     */
    CurrentViewTab save(SysViewTab sysViewTab);
}
