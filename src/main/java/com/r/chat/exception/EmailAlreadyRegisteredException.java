package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class EmailAlreadyRegisteredException extends BusinessException {
    public EmailAlreadyRegisteredException(String message) {
        super(message, ResponseCodeEnum.ALREADY_EXIST.getCode());
    }
}
