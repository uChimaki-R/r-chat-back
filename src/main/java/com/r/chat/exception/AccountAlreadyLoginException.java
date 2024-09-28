package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class AccountAlreadyLoginException extends BusinessException {
    public AccountAlreadyLoginException(String message) {
        super(message, ResponseCodeEnum.ALREADY_EXIST.getCode());
    }
}
