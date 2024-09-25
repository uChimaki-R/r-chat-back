package com.r.chat.exception;

public class PasswordErrorException extends BusinessException {
    public PasswordErrorException() {
    }

    public PasswordErrorException(String message) {
        super(message);
    }
}
