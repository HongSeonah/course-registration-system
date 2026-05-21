package com.example.courseregistration.domain.user.exception;

import com.example.courseregistration.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {
    CREATOR_NOT_FOUND(HttpStatus.NOT_FOUND, "CREATOR_NOT_FOUND", "크리에이터를 찾을 수 없습니다."),
    USER_NOT_CREATOR(HttpStatus.FORBIDDEN, "USER_NOT_CREATOR", "CREATOR 역할의 사용자만 강의를 등록할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    UserErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
