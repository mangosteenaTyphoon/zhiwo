package com.shanzhu.ip.exception;

import com.shanzhu.common.enums.ResultCodeEnum;
import com.shanzhu.common.exception.BaseException;

/**
 * ip 非法异常
 */
public class IpIllegalException extends BaseException {
    public IpIllegalException() {
        super(ResultCodeEnum.IP_ILLEGAL_ERROR, null);
    }
}
