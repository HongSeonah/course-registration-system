package com.example.courseregistration.domain.courseclass.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CourseClassUpdateRequest(
        @NotBlank @Size(max = 120) String title,
        @NotBlank String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Min(1) int capacity,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
