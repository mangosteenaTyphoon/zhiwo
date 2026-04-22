package com.shanzhu.sensitive.exception;

import com.shanzhu.common.enums.ResultCodeEnum;
import com.shanzhu.common.exception.BaseException;

/**
 * 数据脱敏异常
 */
public class SensitiveException extends BaseException {
    public SensitiveException(String message) {
        super(ResultCodeEnum.SENSITIVE_ERROR, ResultCodeEnum.SENSITIVE_ERROR.getDefaultMsg() + "，" + message);
    }
}
