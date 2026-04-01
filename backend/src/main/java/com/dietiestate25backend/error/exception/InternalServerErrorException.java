package com.dietiestate25backend.error.exception;

import com.dietiestate25backend.error.ErrorCode;

public class InternalServerErrorException extends RuntimeException {
    private final ErrorCode errorCode;

    public InternalServerErrorException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public InternalServerErrorException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.name(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}