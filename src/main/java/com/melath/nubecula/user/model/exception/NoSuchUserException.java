package com.melath.nubecula.user.model.exception;

public class NoSuchUserException extends RuntimeException {
    public NoSuchUserException(String message) { super(message); }
}