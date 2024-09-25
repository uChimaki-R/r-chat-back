package com.r.chat.exception;

public class AccountNotExistException extends BusinessException {
    public AccountNotExistException() {
    }

    public AccountNotExistException(String message) {
        super(message);
    }
}
