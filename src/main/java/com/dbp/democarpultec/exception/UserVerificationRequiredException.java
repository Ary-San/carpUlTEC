package com.dbp.democarpultec.exception;

public class UserVerificationRequiredException extends RuntimeException {

    public UserVerificationRequiredException(String message) {
        super(message);
    }
}