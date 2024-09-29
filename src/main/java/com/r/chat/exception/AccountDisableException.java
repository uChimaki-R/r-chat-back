package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class AccountDisableException extends BusinessException {
    public AccountDisableException(String message) {
        super(message, ResponseCodeEnum.REJECTED.getCode());
    }
}
