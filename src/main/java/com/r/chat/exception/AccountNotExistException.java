package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class AccountNotExistException extends BusinessException {
    public AccountNotExistException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
