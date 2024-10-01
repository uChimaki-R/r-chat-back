package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class UserDisableException extends BusinessException {
    public UserDisableException(String message) {
        super(message, ResponseCodeEnum.REJECTED.getCode());
    }
}
