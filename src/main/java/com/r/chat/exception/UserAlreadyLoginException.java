package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class UserAlreadyLoginException extends BusinessException {
    public UserAlreadyLoginException(String message) {
        super(message, ResponseCodeEnum.ALREADY_EXIST.getCode());
    }
}
