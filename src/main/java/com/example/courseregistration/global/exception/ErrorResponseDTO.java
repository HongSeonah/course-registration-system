package com.example.courseregistration.global.exception;

public record ErrorResponseDTO(String code, String message) {

    public static ErrorResponseDTO from(ErrorCode errorCode) {
        return new ErrorResponseDTO(errorCode.getCode(), errorCode.getMessage());
    }

    public static ErrorResponseDTO of(ErrorCode errorCode, String message) {
        return new ErrorResponseDTO(errorCode.getCode(), message);
    }
}
