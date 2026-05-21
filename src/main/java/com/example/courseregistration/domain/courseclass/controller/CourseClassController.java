package com.example.courseregistration.domain.courseclass.controller;

import com.example.courseregistration.domain.courseclass.dto.request.CourseClassCreateRequest;
import com.example.courseregistration.domain.courseclass.dto.request.CourseClassStatusRequest;
import com.example.courseregistration.domain.courseclass.dto.request.CourseClassUpdateRequest;
import com.example.courseregistration.domain.courseclass.dto.response.CourseClassResponse;
import com.example.courseregistration.domain.courseclass.entity.CourseClassStatus;
import com.example.courseregistration.domain.courseclass.service.CourseClassService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/course-classes")
public class CourseClassController {

    private final CourseClassService courseClassService;

    public CourseClassController(CourseClassService courseClassService) {
        this.courseClassService = courseClassService;
    }

    // 강의 생성 API
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseClassResponse create(@Valid @RequestBody CourseClassCreateRequest request) {
        return courseClassService.create(request);
    }

    // 강의 목록 조회 API
    @GetMapping
    public List<CourseClassResponse> findAll(@RequestParam(required = false) CourseClassStatus status) {
        return courseClassService.findAll(status);
    }

    // 강의 상세 조회 API
    @GetMapping("/{courseClassId}")
    public CourseClassResponse findById(@PathVariable Long courseClassId) {
        return courseClassService.findById(courseClassId);
    }

    // 강의 수정 API
    @PutMapping("/{courseClassId}")
    public CourseClassResponse update(@PathVariable Long courseClassId,
                                      @Valid @RequestBody CourseClassUpdateRequest request) {
        return courseClassService.update(courseClassId, request);
    }

    // 강의 상태 변경 API
    @PatchMapping("/{courseClassId}/status")
    public CourseClassResponse changeStatus(@PathVariable Long courseClassId,
                                            @Valid @RequestBody CourseClassStatusRequest request) {
        return courseClassService.changeStatus(courseClassId, request.status());
    }

    // 강의 삭제 API
    @DeleteMapping("/{courseClassId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long courseClassId) {
        courseClassService.delete(courseClassId);
    }
}
