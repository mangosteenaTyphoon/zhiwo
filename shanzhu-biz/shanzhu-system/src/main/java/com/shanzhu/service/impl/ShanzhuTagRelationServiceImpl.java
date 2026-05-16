package com.shanzhu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shanzhu.entity.ShanzhuTag;
import com.shanzhu.entity.ShanzhuTagRelation;
import com.shanzhu.mapper.ShanzhuTagMapper;
import com.shanzhu.mapper.ShanzhuTagRelationMapper;
import com.shanzhu.model.dto.ShanzhuTagRelationDeleteDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationQueryDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationSaveDTO;
import com.shanzhu.model.vo.ShanzhuTagVO;
import com.shanzhu.security.manager.LoginUserContext;
import com.shanzhu.service.ShanzhuTagRelationService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShanzhuTagRelationServiceImpl extends ServiceImpl<ShanzhuTagRelationMapper, ShanzhuTagRelation> implements ShanzhuTagRelationService {

    @Resource
    private ShanzhuTagMapper shanzhuTagMapper;

    @Override
    public void saveRelations(ShanzhuTagRelationSaveDTO saveDTO) {
        deleteByBiz(saveDTO.getBizType(), saveDTO.getBizId(), null);

        if (saveDTO.getTagIds() == null || saveDTO.getTagIds().isEmpty()) {
            return;
        }

        String userId = LoginUserContext.getUserId();
        List<ShanzhuTagRelation> relations = new ArrayList<>();
        saveDTO.getTagIds().stream()
                .filter(StringUtils::hasText)
                .distinct()
                .forEach(tagId -> {
                    ShanzhuTagRelation relation = new ShanzhuTagRelation();
                    relation.setTagId(tagId);
                    relation.setBizType(saveDTO.getBizType());
                    relation.setBizId(saveDTO.getBizId());
                    relation.setUserId(userId);
                    relations.add(relation);
                });

        if (!relations.isEmpty()) {
            saveBatch(relations);
            increaseUsageCount(relations.stream().map(ShanzhuTagRelation::getTagId).toList());
        }
    }

    @Override
    public List<ShanzhuTagVO> queryRelations(ShanzhuTagRelationQueryDTO queryDTO) {
        QueryWrapper<ShanzhuTagRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTagRelation::getBizType, queryDTO.getBizType())
                .eq(ShanzhuTagRelation::getBizId, queryDTO.getBizId());
        List<ShanzhuTagRelation> relations = list(queryWrapper);

        if (relations.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> tagIds = relations.stream().map(ShanzhuTagRelation::getTagId).distinct().toList();
        List<ShanzhuTag> tags = shanzhuTagMapper.selectBatchIds(tagIds);
        List<ShanzhuTagVO> tagVOList = new ArrayList<>();
        tags.forEach(tag -> {
            ShanzhuTagVO tagVO = new ShanzhuTagVO();
            BeanUtils.copyProperties(tag, tagVO);
            tagVOList.add(tagVO);
        });
        return tagVOList;
    }

    @Override
    public void deleteRelations(ShanzhuTagRelationDeleteDTO deleteDTO) {
        deleteByBiz(deleteDTO.getBizType(), deleteDTO.getBizId(), deleteDTO.getTagIds());
    }

    private void deleteByBiz(String bizType, String bizId, List<String> tagIds) {
        QueryWrapper<ShanzhuTagRelation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(ShanzhuTagRelation::getBizType, bizType)
                .eq(ShanzhuTagRelation::getBizId, bizId);

        if (tagIds != null && !tagIds.isEmpty()) {
            queryWrapper.lambda().in(ShanzhuTagRelation::getTagId, tagIds);
        }

        remove(queryWrapper);
    }

    private void increaseUsageCount(List<String> tagIds) {
        tagIds.stream().distinct().forEach(tagId -> {
            UpdateWrapper<ShanzhuTag> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .setSql("usage_count = IFNULL(usage_count, 0) + 1")
                    .eq(ShanzhuTag::getId, tagId);
            shanzhuTagMapper.update(null, updateWrapper);
        });
    }
}
