package com.example.courseregistration.domain.courseclass.service;

import com.example.courseregistration.domain.courseclass.dto.request.CourseClassCreateRequest;
import com.example.courseregistration.domain.courseclass.dto.request.CourseClassUpdateRequest;
import com.example.courseregistration.domain.courseclass.dto.response.CourseClassResponse;
import com.example.courseregistration.domain.courseclass.entity.CourseClass;
import com.example.courseregistration.domain.courseclass.entity.CourseClassStatus;
import com.example.courseregistration.domain.courseclass.exception.CourseClassErrorCode;
import com.example.courseregistration.domain.courseclass.repository.CourseClassRepository;
import com.example.courseregistration.domain.enrollment.entity.EnrollmentStatus;
import com.example.courseregistration.domain.enrollment.repository.EnrollmentRepository;
import com.example.courseregistration.domain.user.entity.User;
import com.example.courseregistration.domain.user.entity.UserRole;
import com.example.courseregistration.domain.user.exception.UserErrorCode;
import com.example.courseregistration.domain.user.repository.UserRepository;
import com.example.courseregistration.global.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CourseClassService {

    private static final List<EnrollmentStatus> OCCUPIED_STATUSES =
            List.copyOf(EnumSet.of(EnrollmentStatus.PENDING, EnrollmentStatus.CONFIRMED));

    private final CourseClassRepository courseClassRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseClassService(CourseClassRepository courseClassRepository,
                              UserRepository userRepository,
                              EnrollmentRepository enrollmentRepository) {
        this.courseClassRepository = courseClassRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // 강의 생성
    @Transactional
    public CourseClassResponse create(CourseClassCreateRequest request) {
        validatePeriod(request.startDate(), request.endDate());
        User creator = getCreator(request.creatorId());

        CourseClass courseClass = CourseClass.builder()
                .creator(creator)
                .title(request.title())
                .description(request.description())
                .price(request.price())
                .capacity(request.capacity())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        return CourseClassResponse.of(courseClassRepository.save(courseClass), 0);
    }

    // 강의 목록 조회
    public List<CourseClassResponse> findAll(CourseClassStatus status) {
        List<CourseClass> courseClasses = status == null
                ? courseClassRepository.findAll()
                : courseClassRepository.findByStatus(status);

        return courseClasses.stream()
                .map(courseClass -> CourseClassResponse.of(courseClass, currentEnrollmentCount(courseClass.getId())))
                .toList();
    }

    // 강의 상세 조회
    public CourseClassResponse findById(Long courseClassId) {
        CourseClass courseClass = getCourseClass(courseClassId);
        return CourseClassResponse.of(courseClass, currentEnrollmentCount(courseClassId));
    }

    // 강의 수정
    @Transactional
    public CourseClassResponse update(Long courseClassId, CourseClassUpdateRequest request) {
        validatePeriod(request.startDate(), request.endDate());
        CourseClass courseClass = getCourseClass(courseClassId);
        long currentEnrollmentCount = currentEnrollmentCount(courseClassId);

        // 현재 신청 인원보다 작은 정원으로 수정 방지
        if (request.capacity() < currentEnrollmentCount) {
            throw new BaseException(CourseClassErrorCode.CAPACITY_BELOW_CURRENT_ENROLLMENTS);
        }

        courseClass.update(
                request.title(),
                request.description(),
                request.price(),
                request.capacity(),
                request.startDate(),
                request.endDate()
        );

        return CourseClassResponse.of(courseClass, currentEnrollmentCount);
    }

    // 강의 상태 변경
    @Transactional
    public CourseClassResponse changeStatus(Long courseClassId, CourseClassStatus status) {
        CourseClass courseClass = getCourseClass(courseClassId);
        courseClass.changeStatus(status);
        return CourseClassResponse.of(courseClass, currentEnrollmentCount(courseClassId));
    }

    // 강의 삭제
    @Transactional
    public void delete(Long courseClassId) {
        CourseClass courseClass = getCourseClass(courseClassId);
        long currentEnrollmentCount = currentEnrollmentCount(courseClassId);

        // 신청 인원이 있는 강의 삭제 방지
        if (currentEnrollmentCount > 0) {
            throw new BaseException(CourseClassErrorCode.COURSE_CLASS_HAS_ENROLLMENTS);
        }

        courseClassRepository.delete(courseClass);
    }

    // 강사 조회
    private User getCreator(Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BaseException(UserErrorCode.CREATOR_NOT_FOUND));

        if (creator.getRole() != UserRole.CREATOR) {
            throw new BaseException(UserErrorCode.USER_NOT_CREATOR);
        }

        return creator;
    }

    // 강의 조회
    private CourseClass getCourseClass(Long courseClassId) {
        return courseClassRepository.findById(courseClassId)
                .orElseThrow(() -> new BaseException(CourseClassErrorCode.COURSE_CLASS_NOT_FOUND));
    }

    // 현재 신청 인원 조회
    private long currentEnrollmentCount(Long courseClassId) {
        return enrollmentRepository.countByCourseClassIdAndStatusIn(courseClassId, OCCUPIED_STATUSES);
    }

    // 수강 기간 검증
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new BaseException(CourseClassErrorCode.INVALID_COURSE_CLASS_PERIOD);
        }
    }
}
