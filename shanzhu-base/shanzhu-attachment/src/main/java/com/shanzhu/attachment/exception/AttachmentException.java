package com.shanzhu.attachment.exception;


import com.shanzhu.common.enums.ResultCodeEnum;
import com.shanzhu.common.exception.BaseException;

/**
 * 附件相关异常
 */
public class AttachmentException extends BaseException {

    public AttachmentException() {
        super(ResultCodeEnum.FILE_ERROR, null);
    }

    public AttachmentException(String message) {
        super(ResultCodeEnum.FILE_ERROR, message, null);
    }

    public AttachmentException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum, null);
    }
}
