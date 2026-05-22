package com.example.courseregistration.domain.enrollment.service;

import com.example.courseregistration.domain.courseclass.entity.CourseClass;
import com.example.courseregistration.domain.courseclass.entity.CourseClassStatus;
import com.example.courseregistration.domain.courseclass.repository.CourseClassRepository;
import com.example.courseregistration.domain.enrollment.dto.request.EnrollmentActionRequest;
import com.example.courseregistration.domain.enrollment.dto.request.EnrollmentCreateRequest;
import com.example.courseregistration.domain.enrollment.dto.response.EnrollmentResponse;
import com.example.courseregistration.domain.enrollment.entity.Enrollment;
import com.example.courseregistration.domain.enrollment.entity.EnrollmentStatus;
import com.example.courseregistration.domain.enrollment.exception.EnrollmentErrorCode;
import com.example.courseregistration.domain.enrollment.repository.EnrollmentRepository;
import com.example.courseregistration.domain.schedule.repository.ClassScheduleRepository;
import com.example.courseregistration.domain.user.entity.User;
import com.example.courseregistration.domain.user.entity.UserRole;
import com.example.courseregistration.domain.user.repository.UserRepository;
import com.example.courseregistration.global.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseClassRepository courseClassRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClassScheduleRepository scheduleRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    @Test
    void create_returnsPending_whenCapacityIsAvailable() {
        // given
        CourseClass courseClass = courseClass(3L, 2, CourseClassStatus.OPEN);
        User classmate = classmate(2L);

        when(courseClassRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(courseClass));
        when(courseClassRepository.findById(3L)).thenReturn(Optional.of(courseClass));
        when(scheduleRepository.findByCourseClassId(3L)).thenReturn(List.of());
        when(userRepository.findById(2L)).thenReturn(Optional.of(classmate));
        when(enrollmentRepository.findByCourseClassAndClassmate(eq(3L), eq(2L), anyCollection()))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.findConflictingEnrollments(eq(2L), anyCollection(), eq(3L)))
                .thenReturn(List.of());
        when(enrollmentRepository.countByCourseClassIdAndStatusIn(eq(3L), anyCollection()))
                .thenReturn(0L);
        when(enrollmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        EnrollmentResponse response = enrollmentService.create(3L, new EnrollmentCreateRequest(2L));

        // then
        assertThat(response.status()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(response.waitlistOrder()).isNull();
        assertThat(response.courseClassId()).isEqualTo(3L);
        assertThat(response.classmateId()).isEqualTo(2L);
    }

    @Test
    void create_returnsWaitlisted_whenCapacityIsExceeded() {
        // given
        CourseClass courseClass = courseClass(3L, 1, CourseClassStatus.OPEN);
        User classmate = classmate(2L);
        Enrollment waitlisted1 = enrollment(10L, courseClass, classmate(9L), EnrollmentStatus.WAITLISTED, 1, null);
        Enrollment waitlisted2 = enrollment(11L, courseClass, classmate(8L), EnrollmentStatus.WAITLISTED, 2, null);

        when(courseClassRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(courseClass));
        when(courseClassRepository.findById(3L)).thenReturn(Optional.of(courseClass));
        when(scheduleRepository.findByCourseClassId(3L)).thenReturn(List.of());
        when(userRepository.findById(2L)).thenReturn(Optional.of(classmate));
        when(enrollmentRepository.findByCourseClassAndClassmate(eq(3L), eq(2L), anyCollection()))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.findConflictingEnrollments(eq(2L), anyCollection(), eq(3L)))
                .thenReturn(List.of());
        when(enrollmentRepository.countByCourseClassIdAndStatusIn(eq(3L), anyCollection()))
                .thenReturn(1L);
        when(enrollmentRepository.findWaitlistEnrollmentsForUpdate(3L, EnrollmentStatus.WAITLISTED))
                .thenReturn(List.of(waitlisted1, waitlisted2));
        when(enrollmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        EnrollmentResponse response = enrollmentService.create(3L, new EnrollmentCreateRequest(2L));

        // then
        assertThat(response.status()).isEqualTo(EnrollmentStatus.WAITLISTED);
        assertThat(response.waitlistOrder()).isEqualTo(3);
    }

    @Test
    void cancel_throwsWhenConfirmedEnrollmentPeriodExpired() {
        // given
        CourseClass courseClass = courseClass(4L, 1, CourseClassStatus.OPEN);
        User classmate = classmate(2L);
        Enrollment enrollment = enrollment(1L, courseClass, classmate, EnrollmentStatus.CONFIRMED, null, LocalDateTime.now().minusDays(8));

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(courseClassRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(courseClass));

        // when & then
        assertThatThrownBy(() -> enrollmentService.cancel(1L, new EnrollmentActionRequest(2L)))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(EnrollmentErrorCode.CANCELLATION_PERIOD_EXPIRED);

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void cancel_promotesFirstWaitlistAndReordersRemainingWaitlist() {
        // given
        CourseClass courseClass = courseClass(4L, 1, CourseClassStatus.OPEN);
        User classmate = classmate(2L);
        Enrollment target = enrollment(1L, courseClass, classmate, EnrollmentStatus.PENDING, null, null);
        Enrollment waitlisted1 = enrollment(10L, courseClass, classmate(3L), EnrollmentStatus.WAITLISTED, 1, null);
        Enrollment waitlisted2 = enrollment(11L, courseClass, classmate(4L), EnrollmentStatus.WAITLISTED, 2, null);

        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(target));
        when(courseClassRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(courseClass));
        when(enrollmentRepository.findWaitlistEnrollmentsForUpdate(4L, EnrollmentStatus.WAITLISTED))
                .thenReturn(List.of(waitlisted1, waitlisted2), List.of(waitlisted2));
        when(enrollmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);

        // when
        enrollmentService.cancel(1L, new EnrollmentActionRequest(2L));

        // then
        verify(enrollmentRepository, org.mockito.Mockito.times(3)).save(captor.capture());

        List<Enrollment> savedEnrollments = captor.getAllValues();
        assertThat(savedEnrollments).anySatisfy(saved -> {
            assertThat(saved.getId()).isEqualTo(1L);
            assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
        });
        assertThat(savedEnrollments).anySatisfy(saved -> {
            assertThat(saved.getId()).isEqualTo(10L);
            assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
            assertThat(saved.getWaitlistOrder()).isNull();
        });
        assertThat(savedEnrollments).anySatisfy(saved -> {
            assertThat(saved.getId()).isEqualTo(11L);
            assertThat(saved.getStatus()).isEqualTo(EnrollmentStatus.WAITLISTED);
            assertThat(saved.getWaitlistOrder()).isEqualTo(1);
        });
    }

    @Test
    void findMyEnrollments_returnsPagedResults() {
        // given
        User classmate = classmate(2L);
        CourseClass courseClass = courseClass(4L, 1, CourseClassStatus.OPEN);
        Enrollment first = enrollment(1L, courseClass, classmate, EnrollmentStatus.PENDING, null, null);
        Enrollment second = enrollment(2L, courseClass, classmate, EnrollmentStatus.WAITLISTED, 1, null);
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findById(2L)).thenReturn(Optional.of(classmate));
        when(enrollmentRepository.findByClassmateId(2L, pageable))
                .thenReturn(new PageImpl<>(List.of(first, second), pageable, 2));

        // when
        Page<EnrollmentResponse> page = enrollmentService.findMyEnrollments(2L, pageable);

        // then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).status()).isEqualTo(EnrollmentStatus.PENDING);
        assertThat(page.getContent().get(1).status()).isEqualTo(EnrollmentStatus.WAITLISTED);
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    private CourseClass courseClass(Long id, int capacity, CourseClassStatus status) {
        return CourseClass.builder()
                .id(id)
                .creator(classmate(1L))
                .title("강의")
                .description("설명")
                .price(BigDecimal.valueOf(100000))
                .capacity(capacity)
                .startDate(LocalDate.of(2026, 6, 1))
                .endDate(LocalDate.of(2026, 6, 30))
                .status(status)
                .build();
    }

    private User classmate(Long id) {
        return User.builder()
                .id(id)
                .name("학생" + id)
                .role(UserRole.CLASSMATE)
                .build();
    }

    private Enrollment enrollment(Long id, CourseClass courseClass, User classmate, EnrollmentStatus status, Integer waitlistOrder, LocalDateTime paidAt) {
        return Enrollment.builder()
                .id(id)
                .courseClass(courseClass)
                .classmate(classmate)
                .status(status)
                .waitlistOrder(waitlistOrder)
                .appliedAt(LocalDateTime.of(2026, 5, 22, 1, 0))
                .paidAt(paidAt)
                .cancelledAt(null)
                .build();
    }
}
