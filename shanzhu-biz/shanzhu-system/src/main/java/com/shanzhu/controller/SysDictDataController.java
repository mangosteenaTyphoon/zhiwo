package com.shanzhu.controller;

import com.shanzhu.common.enums.ResultCodeEnum;
import com.shanzhu.common.model.response.ApiResponseModel;
import com.shanzhu.common.model.response.basecontroller.ApiResponseController;
import com.shanzhu.dict.model.DictDataModel;
import com.shanzhu.entity.SysDictData;
import com.shanzhu.log.annotation.Log;
import com.shanzhu.log.enums.LogTypeEnum;
import com.shanzhu.model.dto.SysDictDataDTO;
import com.shanzhu.service.SysDictDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "字典数据")
@Validated
@RestController
@RequestMapping("system/dictData")
public class SysDictDataController extends ApiResponseController {

    @Resource
    private SysDictDataService sysDictDataService;

    @Operation(summary = "列表查询")
    @PostMapping("list")
    public ApiResponseModel<List<SysDictData>> queryListByTypeCode(@RequestBody SysDictDataDTO dictDataDTO) {
        if (!StringUtils.hasText(dictDataDTO.getDictTypeCode())) {
            return error(ResultCodeEnum.ERROR,"数据字典类型id为空");
        }
        return success(sysDictDataService.queryList(dictDataDTO));
    }

    @Operation(summary = "根据字典类型获取选项")
    @GetMapping("option/{dictTypeCode}")
    public ApiResponseModel<List<DictDataModel>> queryDictOptionList(@PathVariable("dictTypeCode") String dictTypeCode) {
        return success(sysDictDataService.queryDictOptionList(dictTypeCode));
    }

    @Operation(summary = "根据字典类型批量获取选项")
    @PostMapping("option")
    public ApiResponseModel<Map<String, List<DictDataModel>>> queryDictOptionList(@RequestBody List<String> dictTypeCodeList) {
        return success(sysDictDataService.queryDictOptionList(dictTypeCodeList));
    }

    @Operation(summary = "保存字典数据")
    @PreAuthorize("hasRole('ROLE_admin')")
    @PostMapping
    @Log(description = "保存字典数据", type = LogTypeEnum.SAVE)
    public ApiResponseModel<String> save(@RequestBody @Validated SysDictData sysDictData) {
        return success(sysDictDataService.save(sysDictData));
    }

    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasRole('ROLE_admin')")
    @DeleteMapping
    @Log(description = "删除字典数据", type = LogTypeEnum.DELETE)
    public ApiResponseModel<String> delete(@RequestBody @NotEmpty(message = "请选择字段数据") List<String> ids) {
        sysDictDataService.deleteByIds(ids);
        return success();
    }
}
