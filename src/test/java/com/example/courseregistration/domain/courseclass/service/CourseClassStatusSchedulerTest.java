package com.example.courseregistration.domain.courseclass.service;

import com.example.courseregistration.domain.courseclass.entity.CourseClass;
import com.example.courseregistration.domain.courseclass.entity.CourseClassStatus;
import com.example.courseregistration.domain.courseclass.repository.CourseClassRepository;
import com.example.courseregistration.domain.user.entity.User;
import com.example.courseregistration.domain.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseClassStatusSchedulerTest {

    @Mock
    private CourseClassRepository courseClassRepository;

    @InjectMocks
    private CourseClassStatusScheduler scheduler;

    @Test
    void closeExpiredOpenCourseClasses_changesOpenClassesToClosed() {
        // given
        CourseClass expired = CourseClass.builder()
                .id(1L)
                .creator(User.builder().id(1L).name("강사").role(UserRole.CREATOR).build())
                .title("강의")
                .description("설명")
                .price(BigDecimal.valueOf(100000))
                .capacity(10)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(10))
                .status(CourseClassStatus.OPEN)
                .build();

        when(courseClassRepository.findByStatusAndStartDateBefore(CourseClassStatus.OPEN, LocalDate.now()))
                .thenReturn(List.of(expired));

        // when
        scheduler.closeExpiredOpenCourseClasses();

        // then
        assertThat(expired.getStatus()).isEqualTo(CourseClassStatus.CLOSED);
    }
}
