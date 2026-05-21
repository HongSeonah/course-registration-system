package com.example.courseregistration.domain.schedule.controller;

import com.example.courseregistration.domain.schedule.dto.request.ClassScheduleCreateRequest;
import com.example.courseregistration.domain.schedule.dto.request.ClassScheduleUpdateRequest;
import com.example.courseregistration.domain.schedule.dto.response.ClassScheduleResponse;
import com.example.courseregistration.domain.schedule.service.ClassScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClassScheduleController {

    private final ClassScheduleService classScheduleService;

    public ClassScheduleController(ClassScheduleService classScheduleService) {
        this.classScheduleService = classScheduleService;
    }

    // POST /api/course-classes/{courseClassId}/schedules
    @PostMapping("/course-classes/{courseClassId}/schedules")
    public ResponseEntity<ClassScheduleResponse> create(
            @PathVariable Long courseClassId,
            @Valid @RequestBody ClassScheduleCreateRequest request) {
        ClassScheduleResponse response = classScheduleService.create(courseClassId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/course-classes/{courseClassId}/schedules
    @GetMapping("/course-classes/{courseClassId}/schedules")
    public ResponseEntity<List<ClassScheduleResponse>> findAll(
            @PathVariable Long courseClassId) {
        List<ClassScheduleResponse> responses = classScheduleService.findAllByCourseClassId(courseClassId);
        return ResponseEntity.ok(responses);
    }

    // PUT /api/schedules/{scheduleId}
    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ClassScheduleResponse> update(
            @PathVariable Long scheduleId,
            @Valid @RequestBody ClassScheduleUpdateRequest request) {
        ClassScheduleResponse response = classScheduleService.update(scheduleId, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/schedules/{scheduleId}
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long scheduleId) {
        classScheduleService.delete(scheduleId);
        return ResponseEntity.noContent().build();
    }
}
