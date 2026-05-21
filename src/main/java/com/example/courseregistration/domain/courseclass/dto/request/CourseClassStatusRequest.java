package com.example.courseregistration.domain.courseclass.dto.request;

import com.example.courseregistration.domain.courseclass.entity.CourseClassStatus;
import jakarta.validation.constraints.NotNull;

public record CourseClassStatusRequest(@NotNull CourseClassStatus status) {
}
