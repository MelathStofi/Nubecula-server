package com.melath.nubecula.user.model.exception;

import org.springframework.security.core.AuthenticationException;

public class UsernameAlreadyExistsException extends AuthenticationException {

    public UsernameAlreadyExistsException() {
        super("Username already exists.");
    }

}