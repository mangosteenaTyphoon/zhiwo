package com.shanzhu.excel.exception;

import com.shanzhu.common.enums.ResultCodeEnum;
import com.shanzhu.common.exception.BaseException;
import com.shanzhu.common.utils.json.JsonUtils;

import java.util.Collections;
import java.util.List;

/**
 * excel 导入异常
 */
public class ExcelImportException extends BaseException {

    public ExcelImportException(String message) {
        super(ResultCodeEnum.EXCEL_IMPORT_ERROR, JsonUtils.toJson(Collections.singletonList(message)));
    }

    public ExcelImportException(List<String> errMessages) {
        super(ResultCodeEnum.EXCEL_IMPORT_ERROR, JsonUtils.toJson(errMessages));
    }
}
