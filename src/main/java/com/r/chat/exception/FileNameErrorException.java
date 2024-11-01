package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class FileNameErrorException extends BusinessException{
    public FileNameErrorException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
