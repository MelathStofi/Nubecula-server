package com.melath.nubecula.user.model.exception;

import org.springframework.security.core.AuthenticationException;

public class EmailAlreadyExistsException extends AuthenticationException {

    public EmailAlreadyExistsException() {
        super("Email already exists.");
    }
}
