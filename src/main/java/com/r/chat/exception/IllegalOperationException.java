package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class IllegalOperationException extends BusinessException{
    public IllegalOperationException(String message) {
        super(message, ResponseCodeEnum.REJECTED.getCode());
    }
}
