package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class BeingBlockedException extends BusinessException {
    public BeingBlockedException(String message) {
        super(message, ResponseCodeEnum.REJECTED.getCode());
    }
}
