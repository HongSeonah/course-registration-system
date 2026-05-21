package com.example.courseregistration.domain.enrollment.service;

import com.example.courseregistration.domain.courseclass.entity.CourseClass;
import com.example.courseregistration.domain.courseclass.entity.CourseClassStatus;
import com.example.courseregistration.domain.courseclass.exception.CourseClassErrorCode;
import com.example.courseregistration.domain.courseclass.repository.CourseClassRepository;
import com.example.courseregistration.domain.enrollment.dto.request.EnrollmentActionRequest;
import com.example.courseregistration.domain.enrollment.dto.request.EnrollmentCreateRequest;
import com.example.courseregistration.domain.enrollment.dto.response.EnrollmentResponse;
import com.example.courseregistration.domain.enrollment.entity.Enrollment;
import com.example.courseregistration.domain.enrollment.entity.EnrollmentStatus;
import com.example.courseregistration.domain.enrollment.exception.EnrollmentErrorCode;
import com.example.courseregistration.domain.enrollment.repository.EnrollmentRepository;
import com.example.courseregistration.domain.schedule.entity.ClassSchedule;
import com.example.courseregistration.domain.schedule.repository.ClassScheduleRepository;
import com.example.courseregistration.domain.user.entity.User;
import com.example.courseregistration.domain.user.exception.UserErrorCode;
import com.example.courseregistration.domain.user.repository.UserRepository;
import com.example.courseregistration.global.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class EnrollmentService {

    private static final List<EnrollmentStatus> SCHEDULE_CONFLICT_STATUSES =
            List.copyOf(EnumSet.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED, EnrollmentStatus.WAITLISTED));

    private static final List<EnrollmentStatus> OCCUPIED_STATUSES =
            List.copyOf(EnumSet.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED));

    private final EnrollmentRepository enrollmentRepository;
    private final CourseClassRepository courseClassRepository;
    private final UserRepository userRepository;
    private final ClassScheduleRepository scheduleRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseClassRepository courseClassRepository,
                             UserRepository userRepository,
                             ClassScheduleRepository scheduleRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseClassRepository = courseClassRepository;
        this.userRepository = userRepository;
        this.scheduleRepository = scheduleRepository;
    }

    // 수강 신청
    @Transactional
    public EnrollmentResponse create(Long courseClassId, EnrollmentCreateRequest request) {
        CourseClass courseClass = getCourseClass(courseClassId);
        User classmate = getClassmate(request.classmateId());

        // 강의 OPEN인지 확인
        if (courseClass.getStatus() != CourseClassStatus.OPEN) {
            throw new BaseException(EnrollmentErrorCode.COURSE_CLASS_NOT_OPEN);
        }

        // 이미 신청/대기 중인지 확인
        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByCourseClassAndClassmate(
                courseClassId,
                request.classmateId(),
                List.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED, EnrollmentStatus.WAITLISTED)
        );
        if (existingEnrollment.isPresent()) {
            throw new BaseException(EnrollmentErrorCode.ALREADY_ENROLLED);
        }

        // 시간표 중복 확인
        List<Enrollment> conflictingEnrollments = enrollmentRepository.findConflictingEnrollments(
                request.classmateId(),
                SCHEDULE_CONFLICT_STATUSES,
                courseClassId
        );
        checkScheduleConflict(courseClassId, conflictingEnrollments);

        // 4. 현재 신청 인원 확인 -> 5. 상태 결정
        long currentCount = enrollmentRepository.countByCourseClassIdAndStatusIn(courseClassId, OCCUPIED_STATUSES);
        EnrollmentStatus status;
        Integer waitlistOrder = null;

        if (currentCount < courseClass.getCapacity()) {
            // 정원 미만 -> PENDING
            status = EnrollmentStatus.PENDING;
        } else {
            // 정원 초과 -> WAITLISTED
            status = EnrollmentStatus.WAITLISTED;
            waitlistOrder = getNextWaitlistOrder(courseClassId);
        }

        Enrollment enrollment = Enrollment.builder()
                .courseClass(courseClass)
                .classmate(classmate)
                .status(status)
                .waitlistOrder(waitlistOrder)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.from(saved);
    }

    // 결제 확정
    @Transactional
    public EnrollmentResponse confirm(Long enrollmentId, EnrollmentActionRequest request) {
        Enrollment enrollment = getEnrollment(enrollmentId);

        // classmateId 검증
        if (!enrollment.getClassmate().getId().equals(request.classmateId())) {
            throw new BaseException(EnrollmentErrorCode.INVALID_CLASSMATE_ID);
        }

        // PENDING 상태 확인
        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new BaseException(EnrollmentErrorCode.INVALID_ENROLLMENT_STATUS);
        }

        // PENDING -> CONFIRMED
        enrollment = Enrollment.builder()
                .id(enrollment.getId())
                .courseClass(enrollment.getCourseClass())
                .classmate(enrollment.getClassmate())
                .status(EnrollmentStatus.CONFIRMED)
                .waitlistOrder(enrollment.getWaitlistOrder())
                .appliedAt(enrollment.getAppliedAt())
                .paidAt(LocalDateTime.now())
                .cancelledAt(enrollment.getCancelledAt())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();

        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    // 수강 취소
    @Transactional
    public void cancel(Long enrollmentId, EnrollmentActionRequest request) {
        Enrollment enrollment = getEnrollment(enrollmentId);

        // classmateId 검증
        if (!enrollment.getClassmate().getId().equals(request.classmateId())) {
            throw new BaseException(EnrollmentErrorCode.INVALID_CLASSMATE_ID);
        }

        // PENDING / CONFIRMED / WAITLISTED 상태 확인
        if (!List.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED, EnrollmentStatus.WAITLISTED)
                .contains(enrollment.getStatus())) {
            throw new BaseException(EnrollmentErrorCode.INVALID_ENROLLMENT_STATUS);
        }

        EnrollmentStatus previousStatus = enrollment.getStatus();
        Long courseClassId = enrollment.getCourseClass().getId();

        // CANCELLED로 변경
        Enrollment cancelled = Enrollment.builder()
                .id(enrollment.getId())
                .courseClass(enrollment.getCourseClass())
                .classmate(enrollment.getClassmate())
                .status(EnrollmentStatus.CANCELLED)
                .waitlistOrder(enrollment.getWaitlistOrder())
                .appliedAt(enrollment.getAppliedAt())
                .paidAt(enrollment.getPaidAt())
                .cancelledAt(LocalDateTime.now())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();

        enrollmentRepository.save(cancelled);

        // 취소 후 대기열 처리
        if (previousStatus == EnrollmentStatus.PENDING || previousStatus == EnrollmentStatus.CONFIRMED) {
            promoteWaitlistEnrollment(courseClassId);
        }
    }

    // 내 수강 신청 목록 조회
    public List<EnrollmentResponse> findMyEnrollments(Long classmateId) {
        getClassmate(classmateId); // 사용자 존재 확인
        return enrollmentRepository.findByClassmateId(classmateId).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    // 강의별 수강 신청 목록 조회
    public List<EnrollmentResponse> findByCourseClass(Long courseClassId) {
        getCourseClass(courseClassId); // 강의 존재 확인
        return enrollmentRepository.findByCourseClassId(courseClassId).stream()
                .map(EnrollmentResponse::from)
                .toList();
    }

    // 강의 조회
    private CourseClass getCourseClass(Long courseClassId) {
        return courseClassRepository.findById(courseClassId)
                .orElseThrow(() -> new BaseException(CourseClassErrorCode.COURSE_CLASS_NOT_FOUND));
    }

    // 사용자 조회
    private User getClassmate(Long classmateId) {
        return userRepository.findById(classmateId)
                .orElseThrow(() -> new BaseException(UserErrorCode.CLASSMATE_NOT_FOUND));
    }

    // 신청 조회
    private Enrollment getEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BaseException(EnrollmentErrorCode.ENROLLMENT_NOT_FOUND));
    }

    // 시간표 충돌 확인
    private void checkScheduleConflict(Long courseClassId, List<Enrollment> conflictingEnrollments) {
        List<ClassSchedule> newCourseSchedules = scheduleRepository.findByCourseClassId(courseClassId);

        for (Enrollment conflicting : conflictingEnrollments) {
            List<ClassSchedule> existingSchedules = scheduleRepository.findByCourseClassId(conflicting.getCourseClass().getId());

            for (ClassSchedule newSchedule : newCourseSchedules) {
                for (ClassSchedule existingSchedule : existingSchedules) {
                    if (hasTimeConflict(newSchedule, existingSchedule)) {
                        throw new BaseException(EnrollmentErrorCode.SCHEDULE_CONFLICT);
                    }
                }
            }
        }
    }

    // 시간 중복 검사
    private boolean hasTimeConflict(ClassSchedule schedule1, ClassSchedule schedule2) {
        if (!schedule1.getDayOfWeek().equals(schedule2.getDayOfWeek())) {
            return false;
        }

        return schedule1.getStartTime().isBefore(schedule2.getEndTime()) &&
                schedule2.getStartTime().isBefore(schedule1.getEndTime());
    }

    // 대기열 다음 순번 계산
    private Integer getNextWaitlistOrder(Long courseClassId) {
        List<Enrollment> waitlist = enrollmentRepository.findWaitlistEnrollments(courseClassId, EnrollmentStatus.WAITLISTED);
        if (waitlist.isEmpty()) {
            return 1;
        }
        return waitlist.stream()
                .mapToInt(Enrollment::getWaitlistOrder)
                .max()
                .orElse(0) + 1;
    }

    // 대기열 1순위 승격
    private void promoteWaitlistEnrollment(Long courseClassId) {
        Optional<Enrollment> firstWaitlist = enrollmentRepository.findFirstWaitlistEnrollment(
                courseClassId,
                EnrollmentStatus.WAITLISTED
        );

        if (firstWaitlist.isPresent()) {
            Enrollment enrollment = firstWaitlist.get();
            // WAITLISTED -> PENDING
            Enrollment promoted = Enrollment.builder()
                    .id(enrollment.getId())
                    .courseClass(enrollment.getCourseClass())
                    .classmate(enrollment.getClassmate())
                    .status(EnrollmentStatus.PENDING)
                    .waitlistOrder(null)
                    .appliedAt(enrollment.getAppliedAt())
                    .paidAt(enrollment.getPaidAt())
                    .cancelledAt(enrollment.getCancelledAt())
                    .createdAt(enrollment.getCreatedAt())
                    .updatedAt(enrollment.getUpdatedAt())
                    .build();

            enrollmentRepository.save(promoted);

            // 나머지 대기열 순번 재정렬
            reorderWaitlist(courseClassId);
        }
    }

    // 대기열 순번 재정렬
    private void reorderWaitlist(Long courseClassId) {
        List<Enrollment> waitlist = enrollmentRepository.findWaitlistEnrollments(courseClassId, EnrollmentStatus.WAITLISTED);

        int order = 1;
        for (Enrollment enrollment : waitlist) {
            Enrollment updated = Enrollment.builder()
                    .id(enrollment.getId())
                    .courseClass(enrollment.getCourseClass())
                    .classmate(enrollment.getClassmate())
                    .status(enrollment.getStatus())
                    .waitlistOrder(order)
                    .appliedAt(enrollment.getAppliedAt())
                    .paidAt(enrollment.getPaidAt())
                    .cancelledAt(enrollment.getCancelledAt())
                    .createdAt(enrollment.getCreatedAt())
                    .updatedAt(enrollment.getUpdatedAt())
                    .build();

            enrollmentRepository.save(updated);
            order++;
        }
    }
}
