package com.example.courseregistration.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponseDTO> handleBaseException(BaseException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseDTO.from(errorCode));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse(GlobalErrorCode.VALIDATION_FAILED.getMessage());
        return ResponseEntity
                .status(GlobalErrorCode.VALIDATION_FAILED.getStatus())
                .body(ErrorResponseDTO.of(GlobalErrorCode.VALIDATION_FAILED, message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatchException() {
        return ResponseEntity
                .status(GlobalErrorCode.BAD_REQUEST.getStatus())
                .body(ErrorResponseDTO.from(GlobalErrorCode.BAD_REQUEST));
    }
}
