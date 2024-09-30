package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class GroupNotExistException extends BusinessException {
    public GroupNotExistException(String message) {
        super(message, ResponseCodeEnum.PARAMETERS_ERROR.getCode());
    }
}
