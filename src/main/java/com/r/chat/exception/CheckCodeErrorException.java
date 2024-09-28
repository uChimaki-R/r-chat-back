package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class CheckCodeErrorException extends BusinessException {
    public CheckCodeErrorException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
