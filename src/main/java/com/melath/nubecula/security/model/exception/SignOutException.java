package com.melath.nubecula.security.model.exception;

import org.springframework.security.core.AuthenticationException;

public class SignOutException extends AuthenticationException {

    public SignOutException() {
        super("Error during signing out");
    }
}
