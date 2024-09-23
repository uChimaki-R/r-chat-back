package com.r.chat.exception;

import lombok.Getter;

@Getter
public class BusinessException extends BaseException {
    private Integer code;

    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Integer code) {
        super(message);
        this.code = code;
    }
}
