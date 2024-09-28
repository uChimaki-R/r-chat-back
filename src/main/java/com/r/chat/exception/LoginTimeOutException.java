package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class LoginTimeOutException extends BusinessException {
    public LoginTimeOutException(String message) {
        super(message, ResponseCodeEnum.UNAUTHORIZED.getCode());
    }
}
