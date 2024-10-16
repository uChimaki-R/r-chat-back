package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class BeautyUserInfoNotExistException extends BusinessException{
    public BeautyUserInfoNotExistException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
