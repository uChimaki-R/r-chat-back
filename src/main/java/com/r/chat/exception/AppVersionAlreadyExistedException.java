package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class AppVersionAlreadyExistedException extends BusinessException{
    public AppVersionAlreadyExistedException(String message) {
        super(message, ResponseCodeEnum.ALREADY_EXIST.getCode());
    }
}
