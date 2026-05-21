package com.example.courseregistration.domain.enrollment.dto.response;

import com.example.courseregistration.domain.enrollment.entity.Enrollment;
import com.example.courseregistration.domain.enrollment.entity.EnrollmentStatus;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        Long courseClassId,
        Long classmateId,
        EnrollmentStatus status,
        Integer waitlistOrder,
        LocalDateTime appliedAt,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourseClass().getId(),
                enrollment.getClassmate().getId(),
                enrollment.getStatus(),
                enrollment.getWaitlistOrder(),
                enrollment.getAppliedAt(),
                enrollment.getPaidAt(),
                enrollment.getCancelledAt(),
                enrollment.getCreatedAt(),
                enrollment.getUpdatedAt()
        );
    }
}
