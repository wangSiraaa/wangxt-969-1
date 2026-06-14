package com.eldercare.common.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;
    private final List<String> errors;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
        this.errors = new ArrayList<>();
    }

    public BusinessException(String message, int code) {
        super(message);
        this.code = code;
        this.message = message;
        this.errors = new ArrayList<>();
    }

    public BusinessException(String message, List<String> errors, int code) {
        super(message);
        this.code = code;
        this.message = message;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.errors = new ArrayList<>();
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.errors = new ArrayList<>();
    }
}
