package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class UserNotExistException extends BusinessException {
    public UserNotExistException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
