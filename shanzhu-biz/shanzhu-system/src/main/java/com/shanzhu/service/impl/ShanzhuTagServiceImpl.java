package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuTag;
import com.shanzhu.mapper.ShanzhuTagMapper;
import com.shanzhu.model.dto.ShanzhuTagQueryDTO;
import com.shanzhu.model.dto.ShanzhuTagSaveDTO;
import com.shanzhu.model.vo.ShanzhuTagVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuTagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShanzhuTagServiceImpl extends ServiceImpl<ShanzhuTagMapper, ShanzhuTag> implements ShanzhuTagService {

    private static final String DEFAULT_TAG_COLOR = "#1677ff";

    @Override
    public List<ShanzhuTagVO> queryList(ShanzhuTagQueryDTO queryDTO) {
        QueryWrapper<ShanzhuTag> queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.lambda().orderByDesc(ShanzhuTag::getUsageCount).orderByDesc(ShanzhuTag::getCreateTime);
        return convertToVOList(list(queryWrapper));
    }

    @Override
    public List<ShanzhuTagVO> search(ShanzhuTagQueryDTO queryDTO) {
        QueryWrapper<ShanzhuTag> queryWrapper = buildQueryWrapper(queryDTO);
        queryWrapper.lambda().orderByDesc(ShanzhuTag::getUsageCount).last("LIMIT 20");
        return convertToVOList(list(queryWrapper));
    }

    @Override
    public String add(ShanzhuTagSaveDTO saveDTO) {
        String tagName = saveDTO.getName().trim();
        String userId = LoginUserContext.getUserId();

        QueryWrapper<ShanzhuTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTag::getUserId, userId)
                .eq(ShanzhuTag::getName, tagName);
        if (StringUtils.hasText(saveDTO.getTagType())) {
            queryWrapper.lambda().eq(ShanzhuTag::getTagType, saveDTO.getTagType());
        }

        ShanzhuTag existingTag = getOne(queryWrapper, false);
        if (existingTag != null) {
            return existingTag.getId();
        }

        ShanzhuTag tag = new ShanzhuTag();
        tag.setUserId(userId);
        tag.setName(tagName);
        tag.setColor(StringUtils.hasText(saveDTO.getColor()) ? saveDTO.getColor() : DEFAULT_TAG_COLOR);
        tag.setTagType(saveDTO.getTagType());
        tag.setUsageCount(0);
        tag.setBuiltIn("0");
        save(tag);
        return tag.getId();
    }

    private QueryWrapper<ShanzhuTag> buildQueryWrapper(ShanzhuTagQueryDTO queryDTO) {
        QueryWrapper<ShanzhuTag> queryWrapper = new QueryWrapper<>();
        if (queryDTO == null) {
            return queryWrapper;
        }

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.lambda().like(ShanzhuTag::getName, queryDTO.getKeyword());
        }
        if (StringUtils.hasText(queryDTO.getTagType())) {
            queryWrapper.lambda().eq(ShanzhuTag::getTagType, queryDTO.getTagType());
        }
        return queryWrapper;
    }

    private List<ShanzhuTagVO> convertToVOList(List<ShanzhuTag> tags) {
        List<ShanzhuTagVO> tagVOList = new ArrayList<>();
        tags.forEach(tag -> {
            ShanzhuTagVO tagVO = new ShanzhuTagVO();
            BeanUtils.copyProperties(tag, tagVO);
            tagVOList.add(tagVO);
        });
        return tagVOList;
    }
}
