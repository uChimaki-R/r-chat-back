package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class AppVersionLTOriginException extends BusinessException{
    public AppVersionLTOriginException(String message) {
        super(message, ResponseCodeEnum.REJECTED.getCode());
    }
}
