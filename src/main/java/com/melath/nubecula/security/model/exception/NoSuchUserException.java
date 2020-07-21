package com.melath.nubecula.security.model.exception;

public class NoSuchUserException extends RuntimeException {
    public NoSuchUserException(String message) { super(message); }
}