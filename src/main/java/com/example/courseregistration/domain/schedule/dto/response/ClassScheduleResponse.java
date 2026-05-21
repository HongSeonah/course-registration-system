package com.example.courseregistration.domain.schedule.dto.response;

import com.example.courseregistration.domain.schedule.entity.ClassSchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ClassScheduleResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String room
) {
    public static ClassScheduleResponse from(ClassSchedule schedule) {
        return new ClassScheduleResponse(
                schedule.getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getRoom()
        );
    }
}
