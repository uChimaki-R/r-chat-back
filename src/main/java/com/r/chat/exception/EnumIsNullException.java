package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class EnumIsNullException extends BusinessException{
    public EnumIsNullException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
