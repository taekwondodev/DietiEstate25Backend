package com.dietiestate25backend.error.exception;

import com.dietiestate25backend.error.ErrorCode;

public class ConflictException extends RuntimeException {
    private final ErrorCode errorCode;

    public ConflictException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}