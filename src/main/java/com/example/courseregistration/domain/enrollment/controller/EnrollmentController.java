package com.example.courseregistration.domain.enrollment.controller;

import com.example.courseregistration.domain.enrollment.dto.request.EnrollmentActionRequest;
import com.example.courseregistration.domain.enrollment.dto.request.EnrollmentCreateRequest;
import com.example.courseregistration.domain.enrollment.dto.response.EnrollmentResponse;
import com.example.courseregistration.domain.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // 수강 신청
    @PostMapping("/course-classes/{courseClassId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse create(@PathVariable Long courseClassId,
                                     @Valid @RequestBody EnrollmentCreateRequest request) {
        return enrollmentService.create(courseClassId, request);
    }

    // 결제 확정
    @PatchMapping("/enrollments/{enrollmentId}/confirm")
    public EnrollmentResponse confirm(@PathVariable Long enrollmentId,
                                      @Valid @RequestBody EnrollmentActionRequest request) {
        return enrollmentService.confirm(enrollmentId, request);
    }

    // 수강 취소
    @PatchMapping("/enrollments/{enrollmentId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long enrollmentId,
                       @Valid @RequestBody EnrollmentActionRequest request) {
        enrollmentService.cancel(enrollmentId, request);
    }

    // 내 수강 신청 목록 조회
    @GetMapping("/users/{userId}/enrollments")
    public Page<EnrollmentResponse> findMyEnrollments(@PathVariable Long userId,
                                                       @PageableDefault(size = 10, sort = "appliedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return enrollmentService.findMyEnrollments(userId, pageable);
    }

    // 강의별 수강 신청 목록 조회
    @GetMapping("/course-classes/{courseClassId}/enrollments")
    public List<EnrollmentResponse> findByCourseClass(@PathVariable Long courseClassId,
                                                        @RequestParam Long creatorId) {
        return enrollmentService.findByCourseClass(courseClassId, creatorId);
    }
}
