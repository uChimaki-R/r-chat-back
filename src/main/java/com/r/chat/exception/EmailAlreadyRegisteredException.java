package com.r.chat.exception;

public class EmailAlreadyRegisteredException extends BusinessException {
    public EmailAlreadyRegisteredException() {
    }

    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
