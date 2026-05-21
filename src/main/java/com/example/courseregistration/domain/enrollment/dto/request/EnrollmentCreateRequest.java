package com.example.courseregistration.domain.enrollment.dto.request;

import jakarta.validation.constraints.NotNull;

// TODO: record 하나로 간략화
public record EnrollmentCreateRequest(
        @NotNull Long classmateId
) {
}
