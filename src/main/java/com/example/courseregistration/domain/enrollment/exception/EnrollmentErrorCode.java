package com.example.courseregistration.domain.enrollment.exception;

import com.example.courseregistration.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum EnrollmentErrorCode implements ErrorCode {
    COURSE_CLASS_NOT_OPEN(HttpStatus.CONFLICT, "COURSE_CLASS_NOT_OPEN", "강의가 OPEN 상태가 아닙니다."),
    ALREADY_ENROLLED(HttpStatus.CONFLICT, "ALREADY_ENROLLED", "이미 수강 중인 강의입니다."),
    ALREADY_APPLIED(HttpStatus.CONFLICT, "ALREADY_APPLIED", "이미 신청 중인 강의입니다."),
    SCHEDULE_CONFLICT(HttpStatus.CONFLICT, "SCHEDULE_CONFLICT", "시간표 충돌이 발생했습니다."),
    CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "CAPACITY_EXCEEDED", "정원이 초과되었습니다."),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ENROLLMENT_NOT_FOUND", "수강 신청 정보를 찾을 수 없습니다."),
    INVALID_ENROLLMENT_STATUS(HttpStatus.CONFLICT, "INVALID_ENROLLMENT_STATUS", "해당 상태에서 요청할 수 없습니다."),
    INVALID_CLASSMATE_ID(HttpStatus.BAD_REQUEST, "INVALID_CLASSMATE_ID", "classmateId와 요청한 사용자가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    EnrollmentErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
