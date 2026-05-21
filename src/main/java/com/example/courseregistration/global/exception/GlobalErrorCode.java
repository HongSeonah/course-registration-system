package com.example.courseregistration.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum GlobalErrorCode implements ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "요청 값이 올바르지 않습니다."),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "요청 값 검증에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버에 문제가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    GlobalErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
