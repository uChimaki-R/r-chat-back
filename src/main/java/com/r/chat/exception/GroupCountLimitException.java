package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class GroupCountLimitException extends BusinessException {
    public GroupCountLimitException(String message) {
        super(message, ResponseCodeEnum.REJECTED.getCode());
    }
}
