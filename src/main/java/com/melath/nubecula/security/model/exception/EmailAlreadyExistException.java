package com.melath.nubecula.security.model.exception;

import org.springframework.security.core.AuthenticationException;

public class EmailAlreadyExistException extends AuthenticationException {

    public EmailAlreadyExistException() {
        super("Email is already exists.");
    }
}
