package com.shanzhu.service;

import com.shanzhu.entity.SysUserRole;

import java.util.List;

public interface SysUserRoleService {

    // 批量插入
    void save(List<SysUserRole> sysUserRoleList);

    // 根据userid删除
    void deleteByUserIds(List<String> userIds);

}
