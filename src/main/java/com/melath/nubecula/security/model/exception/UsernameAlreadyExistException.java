package com.melath.nubecula.security.model.exception;

import org.springframework.security.core.AuthenticationException;

public class UsernameAlreadyExistException extends AuthenticationException {

    public UsernameAlreadyExistException() {
        super("Username is already exists.");
    }

}