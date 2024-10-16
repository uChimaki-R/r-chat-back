package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class BeautyUserIdAlreadyExistedException extends BusinessException{
    public BeautyUserIdAlreadyExistedException(String message) {
        super(message, ResponseCodeEnum.ALREADY_EXIST.getCode());
    }
}
