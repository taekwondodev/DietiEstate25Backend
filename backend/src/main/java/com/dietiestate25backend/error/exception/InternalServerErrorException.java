package com.dietiestate25backend.error.exception;

public class InternalServerErrorException extends RuntimeException {
    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
    public InternalServerErrorException(String message) { super(message);}
}
