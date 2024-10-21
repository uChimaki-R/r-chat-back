package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class AppUpdateNotExistException extends BusinessException{
    public AppUpdateNotExistException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
