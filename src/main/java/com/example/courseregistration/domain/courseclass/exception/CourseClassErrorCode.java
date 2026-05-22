package com.example.courseregistration.domain.courseclass.exception;

import com.example.courseregistration.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CourseClassErrorCode implements ErrorCode {
    INVALID_COURSE_CLASS_PERIOD(HttpStatus.BAD_REQUEST, "INVALID_COURSE_CLASS_PERIOD", "종료일은 시작일보다 빠를 수 없습니다."),
    COURSE_CLASS_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_CLASS_NOT_FOUND", "강의를 찾을 수 없습니다."),
    COURSE_CLASS_ENROLLMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "COURSE_CLASS_ENROLLMENT_ACCESS_DENIED", "해당 강의의 수강생 목록은 크리에이터만 조회할 수 있습니다."),
    CAPACITY_BELOW_CURRENT_ENROLLMENTS(HttpStatus.CONFLICT, "CAPACITY_BELOW_CURRENT_ENROLLMENTS", "정원은 현재 신청 인원보다 작게 변경할 수 없습니다."),
    COURSE_CLASS_HAS_ENROLLMENTS(HttpStatus.CONFLICT, "COURSE_CLASS_HAS_ENROLLMENTS", "현재 신청 인원이 있는 강의는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    CourseClassErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
