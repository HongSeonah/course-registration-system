package com.example.courseregistration.domain.courseclass.dto.response;

import com.example.courseregistration.domain.courseclass.entity.CourseClass;
import com.example.courseregistration.domain.courseclass.entity.CourseClassStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CourseClassResponse(
        Long id,
        Long creatorId,
        String title,
        String description,
        BigDecimal price,
        int capacity,
        LocalDate startDate,
        LocalDate endDate,
        CourseClassStatus status,
        long currentEnrollmentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CourseClassResponse of(CourseClass courseClass, long currentEnrollmentCount) {
        return new CourseClassResponse(
                courseClass.getId(),
                courseClass.getCreator().getId(),
                courseClass.getTitle(),
                courseClass.getDescription(),
                courseClass.getPrice(),
                courseClass.getCapacity(),
                courseClass.getStartDate(),
                courseClass.getEndDate(),
                courseClass.getStatus(),
                currentEnrollmentCount,
                courseClass.getCreatedAt(),
                courseClass.getUpdatedAt()
        );
    }
}
