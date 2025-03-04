package com.directa24.main.challenge.exception;

import org.springframework.http.HttpStatus;

public class RequestException extends RuntimeException {
    final HttpStatus status;
    final Exception cause;

    public RequestException(HttpStatus status, Exception cause) {
        this.status = status;
        this.cause = cause;
    }
}
