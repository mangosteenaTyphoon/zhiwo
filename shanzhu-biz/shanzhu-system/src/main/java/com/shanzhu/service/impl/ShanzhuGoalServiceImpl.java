package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuGoal;
import com.shanzhu.mapper.ShanzhuGoalMapper;
import com.shanzhu.service.ShanzhuGoalService;
import org.springframework.stereotype.Service;

@Service
public class ShanzhuGoalServiceImpl extends ServiceImpl<ShanzhuGoalMapper, ShanzhuGoal> implements ShanzhuGoalService {
}
