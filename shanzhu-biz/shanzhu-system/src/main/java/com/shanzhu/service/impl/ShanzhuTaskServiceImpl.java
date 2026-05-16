package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuTask;
import com.shanzhu.mapper.ShanzhuTaskMapper;
import com.shanzhu.service.ShanzhuTaskService;
import org.springframework.stereotype.Service;

@Service
public class ShanzhuTaskServiceImpl extends ServiceImpl<ShanzhuTaskMapper, ShanzhuTask> implements ShanzhuTaskService {
}
