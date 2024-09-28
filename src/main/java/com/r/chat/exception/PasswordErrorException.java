package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class PasswordErrorException extends BusinessException {
    public PasswordErrorException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
