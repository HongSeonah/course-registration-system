package com.example.courseregistration.domain.schedule.controller;

import com.example.courseregistration.domain.schedule.dto.request.ClassScheduleCreateRequest;
import com.example.courseregistration.domain.schedule.dto.request.ClassScheduleUpdateRequest;
import com.example.courseregistration.domain.schedule.dto.response.ClassScheduleResponse;
import com.example.courseregistration.domain.schedule.service.ClassScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClassScheduleController {

    private final ClassScheduleService classScheduleService;

    public ClassScheduleController(ClassScheduleService classScheduleService) {
        this.classScheduleService = classScheduleService;
    }

    // 시간표 생성 API
    @PostMapping("/course-classes/{courseClassId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public ClassScheduleResponse create(
            @PathVariable Long courseClassId,
            @Valid @RequestBody ClassScheduleCreateRequest request) {
        return classScheduleService.create(courseClassId, request);
    }

    // 시간표 목록 조회 API
    @GetMapping("/course-classes/{courseClassId}/schedules")
    public List<ClassScheduleResponse> findAll(
            @PathVariable Long courseClassId) {
        return classScheduleService.findAllByCourseClassId(courseClassId);
    }

    // 시간표 수정 API
    @PutMapping("/schedules/{scheduleId}")
    public ClassScheduleResponse update(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ClassScheduleUpdateRequest request) {
        return classScheduleService.update(scheduleId, request);
    }

    // 시간표 삭제 API
    @DeleteMapping("/schedules/{scheduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long scheduleId) {
        classScheduleService.delete(scheduleId);
    }
}
