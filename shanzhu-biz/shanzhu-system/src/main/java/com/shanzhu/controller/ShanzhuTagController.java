package com.shanzhu.controller;

import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.model.dto.ShanzhuTagQueryDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationDeleteDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationQueryDTO;
import com.shanzhu.model.dto.ShanzhuTagRelationSaveDTO;
import com.shanzhu.model.dto.ShanzhuTagSaveDTO;
import com.shanzhu.model.vo.ShanzhuTagVO;
import com.shanzhu.service.ShanzhuTagRelationService;
import com.shanzhu.service.ShanzhuTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "山竹标签管理")
@RestController
@RequestMapping("shanzhu/tag")
@Validated
public class ShanzhuTagController extends ApiResponseController {

    @Resource
    private ShanzhuTagService shanzhuTagService;

    @Resource
    private ShanzhuTagRelationService shanzhuTagRelationService;

    @Operation(summary = "查询标签列表")
    @PostMapping("list")
    public ApiResponseModel<List<ShanzhuTagVO>> list(@RequestBody(required = false) ShanzhuTagQueryDTO queryDTO) {
        return success(shanzhuTagService.queryList(queryDTO));
    }

    @Operation(summary = "搜索标签")
    @PostMapping("search")
    public ApiResponseModel<List<ShanzhuTagVO>> search(@RequestBody(required = false) ShanzhuTagQueryDTO queryDTO) {
        return success(shanzhuTagService.search(queryDTO));
    }

    @Operation(summary = "新增标签")
    @PostMapping("add")
    public ApiResponseModel<String> add(@RequestBody @Validated ShanzhuTagSaveDTO saveDTO) {
        return success(shanzhuTagService.add(saveDTO));
    }

    @Operation(summary = "保存业务标签关系")
    @PostMapping("relation/save")
    public ApiResponseModel<String> saveRelation(@RequestBody @Validated ShanzhuTagRelationSaveDTO saveDTO) {
        shanzhuTagRelationService.saveRelations(saveDTO);
        return success();
    }

    @Operation(summary = "查询业务标签关系")
    @PostMapping("relation/list")
    public ApiResponseModel<List<ShanzhuTagVO>> relationList(@RequestBody @Validated ShanzhuTagRelationQueryDTO queryDTO) {
        return success(shanzhuTagRelationService.queryRelations(queryDTO));
    }

    @Operation(summary = "删除业务标签关系")
    @PostMapping("relation/delete")
    public ApiResponseModel<String> deleteRelation(@RequestBody @Validated ShanzhuTagRelationDeleteDTO deleteDTO) {
        shanzhuTagRelationService.deleteRelations(deleteDTO);
        return success();
    }
}
