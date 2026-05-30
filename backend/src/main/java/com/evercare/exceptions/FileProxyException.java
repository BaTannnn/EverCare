package com.evercare.exceptions;

public class FileProxyException extends RuntimeException {
    public FileProxyException(String message) {
        super(message);
    }

    public FileProxyException(String message, Throwable cause) {
        super(message, cause);
    }
}
