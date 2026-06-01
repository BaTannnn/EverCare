package com.evercare.exceptions;

public class AuthenticationRequiredException extends SecurityException {
    public AuthenticationRequiredException(String message) {
        super(message);
    }
}
