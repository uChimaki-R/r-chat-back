package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class FileSaveFailedException extends BusinessException{
    public FileSaveFailedException(String message) {
        super(message, ResponseCodeEnum.INTERNAL_ERROR.getCode());
    }
}
