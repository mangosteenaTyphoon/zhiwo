package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuCategory;
import com.shanzhu.mapper.ShanzhuCategoryMapper;
import com.shanzhu.model.vo.ShanzhuCategoryVO;
import com.shanzhu.service.ShanzhuCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShanzhuCategoryServiceImpl extends ServiceImpl<ShanzhuCategoryMapper, ShanzhuCategory> implements ShanzhuCategoryService {

    private static final List<BuiltInCategory> BUILT_IN_CATEGORIES = List.of(
            new BuiltInCategory("project", "项目建设", "appstore", "#1677ff", 1),
            new BuiltInCategory("growth", "成长学习", "read", "#52c41a", 2),
            new BuiltInCategory("health", "健康运动", "heart", "#fa541c", 3),
            new BuiltInCategory("career", "工作事业", "briefcase", "#722ed1", 4),
            new BuiltInCategory("finance", "财务资产", "wallet", "#faad14", 5),
            new BuiltInCategory("life_habit", "生活习惯", "calendar", "#13c2c2", 6),
            new BuiltInCategory("relationship", "关系家庭", "team", "#eb2f96", 7),
            new BuiltInCategory("self_management", "个人管理", "control", "#2f54eb", 8),
            new BuiltInCategory("other", "其他", "more", "#8c8c8c", 9)
    );

    @Override
    public List<ShanzhuCategoryVO> queryList() {
        initBuiltInCategories();

        QueryWrapper<ShanzhuCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuCategory::getEnabled, "1")
                .orderByAsc(ShanzhuCategory::getSortOrder);
        List<ShanzhuCategory> categories = list(queryWrapper);

        List<ShanzhuCategoryVO> categoryVOList = new ArrayList<>();
        categories.forEach(category -> {
            ShanzhuCategoryVO categoryVO = new ShanzhuCategoryVO();
            BeanUtils.copyProperties(category, categoryVO);
            categoryVOList.add(categoryVO);
        });
        return categoryVOList;
    }

    private void initBuiltInCategories() {
        for (BuiltInCategory builtInCategory : BUILT_IN_CATEGORIES) {
            QueryWrapper<ShanzhuCategory> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(ShanzhuCategory::getCode, builtInCategory.code());
            if (count(queryWrapper) > 0) {
                continue;
            }

            ShanzhuCategory category = new ShanzhuCategory();
            category.setName(builtInCategory.name());
            category.setCode(builtInCategory.code());
            category.setIcon(builtInCategory.icon());
            category.setColor(builtInCategory.color());
            category.setSortOrder(builtInCategory.sortOrder());
            category.setBuiltIn("1");
            category.setEnabled("1");
            save(category);
        }
    }

    private record BuiltInCategory(String code, String name, String icon, String color, Integer sortOrder) {
    }
}
