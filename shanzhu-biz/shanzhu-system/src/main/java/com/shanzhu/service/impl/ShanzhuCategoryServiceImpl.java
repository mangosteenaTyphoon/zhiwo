package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuCategory;
import com.shanzhu.mapper.ShanzhuCategoryMapper;
import com.shanzhu.model.dto.ShanzhuCategorySaveDTO;
import com.shanzhu.model.vo.ShanzhuCategoryVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShanzhuCategoryServiceImpl extends ServiceImpl<ShanzhuCategoryMapper, ShanzhuCategory> implements ShanzhuCategoryService {

    private static final String BUILT_IN_FLAG = "1";
    private static final String CUSTOM_FLAG = "0";
    private static final String ENABLED_FLAG = "1";
    private static final String DEFAULT_CATEGORY_COLOR = "#1677ff";
    private static final int CUSTOM_CATEGORY_SORT_ORDER = 100;

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
                .eq(ShanzhuCategory::getEnabled, ENABLED_FLAG)
                .and(wrapper -> wrapper
                        .eq(ShanzhuCategory::getBuiltIn, BUILT_IN_FLAG)
                        .or()
                        .eq(ShanzhuCategory::getUserId, LoginUserContext.getUserId()))
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

    @Override
    public String addCategory(ShanzhuCategorySaveDTO saveDTO) {
        String categoryName = saveDTO.getName() == null ? "" : saveDTO.getName().trim();
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }

        ShanzhuCategory existingCategory = queryExistingCategory(categoryName);
        if (existingCategory != null) {
            return existingCategory.getId();
        }

        ShanzhuCategory category = new ShanzhuCategory();
        category.setName(categoryName);
        category.setCode("custom_" + System.currentTimeMillis());
        category.setIcon(StringUtils.hasText(saveDTO.getIcon()) ? saveDTO.getIcon() : "appstore");
        category.setColor(StringUtils.hasText(saveDTO.getColor()) ? saveDTO.getColor() : DEFAULT_CATEGORY_COLOR);
        category.setSortOrder(CUSTOM_CATEGORY_SORT_ORDER);
        category.setBuiltIn(CUSTOM_FLAG);
        category.setEnabled(ENABLED_FLAG);
        category.setUserId(LoginUserContext.getUserId());
        save(category);
        return category.getId();
    }

    private ShanzhuCategory queryExistingCategory(String categoryName) {
        QueryWrapper<ShanzhuCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuCategory::getName, categoryName)
                .eq(ShanzhuCategory::getEnabled, ENABLED_FLAG)
                .and(wrapper -> wrapper
                        .eq(ShanzhuCategory::getBuiltIn, BUILT_IN_FLAG)
                        .or()
                        .eq(ShanzhuCategory::getUserId, LoginUserContext.getUserId()));
        return getOne(queryWrapper);
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
            category.setBuiltIn(BUILT_IN_FLAG);
            category.setEnabled(ENABLED_FLAG);
            save(category);
        }
    }

    private record BuiltInCategory(String code, String name, String icon, String color, Integer sortOrder) {
    }
}
