package com.dietiestate25backend.error.exception;

import com.dietiestate25backend.error.ErrorCode;
import lombok.Getter;

@Getter
public class DatabaseErrorException extends RuntimeException {
    private final ErrorCode errorCode;

    public DatabaseErrorException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }
}
