package com.r.chat.exception;

public class AccountAlreadyLoginException extends BusinessException {
    public AccountAlreadyLoginException() {
    }

    public AccountAlreadyLoginException(String message) {
        super(message);
    }
}
