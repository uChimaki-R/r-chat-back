package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class ParameterErrorException extends BusinessException{
    public ParameterErrorException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
