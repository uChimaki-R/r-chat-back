package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class AppVersionLTLatestException extends BusinessException{
    public AppVersionLTLatestException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
