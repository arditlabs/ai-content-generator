package com.aicontentgenerator.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Application-level runtime exception.
 * Carries an HTTP status and a structured ErrorCode for consistent error responses.
 *
 * Usage example:
 *   throw new AppException(HttpStatus.CONFLICT, ErrorCode.EMAIL_ALREADY_EXISTS);
 */
@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final ErrorCode  errorCode;

    public AppException(HttpStatus status, ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.status    = status;
        this.errorCode = errorCode;
    }
}