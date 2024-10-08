package com.r.chat.exception;

import com.r.chat.entity.enums.ResponseCodeEnum;

public class GroupMemberCountLimitException extends BusinessException {
    public GroupMemberCountLimitException(String message) {
        super(message, ResponseCodeEnum.REJECTED.getCode());
    }
}
