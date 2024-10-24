package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class JsonException extends BusinessException{
    public JsonException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
