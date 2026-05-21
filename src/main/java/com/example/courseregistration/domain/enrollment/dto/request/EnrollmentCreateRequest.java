package com.example.courseregistration.domain.enrollment.dto.request;

import jakarta.validation.constraints.NotNull;

public record EnrollmentCreateRequest(
        @NotNull Long classmateId
) {
}
