package com.shanzhu.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ShanzhuTagRelationDeleteDTO {
    /**
     * 业务类型
     */
    @NotBlank(message = "请选择业务类型")
    private String bizType;

    /**
     * 业务ID
     */
    @NotBlank(message = "请选择业务数据")
    private String bizId;

    /**
     * 标签ID列表，为空时删除当前业务对象的全部标签关系
     */
    private List<String> tagIds;
}
