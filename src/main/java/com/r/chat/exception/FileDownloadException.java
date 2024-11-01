package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class FileDownloadException extends BusinessException{
    public FileDownloadException(String message) {
        super(message, ResponseCodeEnum.INTERNAL_ERROR.getCode());
    }
}
