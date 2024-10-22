package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class FileNotExistException extends BusinessException{
    public FileNotExistException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
