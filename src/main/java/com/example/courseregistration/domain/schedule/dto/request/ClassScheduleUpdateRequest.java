package com.example.courseregistration.domain.schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ClassScheduleUpdateRequest(
        @NotNull(message = "요일은 필수입니다.")
        DayOfWeek dayOfWeek,

        @NotNull(message = "시작 시간은 필수입니다.")
        LocalTime startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        LocalTime endTime,

        @NotBlank(message = "강의실은 필수입니다.")
        String room
) {
}
