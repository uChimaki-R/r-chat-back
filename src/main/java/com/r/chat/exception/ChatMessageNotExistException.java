package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class ChatMessageNotExistException extends BusinessException{
    public ChatMessageNotExistException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
