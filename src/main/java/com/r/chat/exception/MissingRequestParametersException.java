package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class MissingRequestParametersException extends BusinessException{
    public MissingRequestParametersException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
