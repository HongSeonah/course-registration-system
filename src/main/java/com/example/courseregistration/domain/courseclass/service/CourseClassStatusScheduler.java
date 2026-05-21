package com.example.courseregistration.domain.courseclass.service;

import com.example.courseregistration.domain.courseclass.entity.CourseClass;
import com.example.courseregistration.domain.courseclass.entity.CourseClassStatus;
import com.example.courseregistration.domain.courseclass.repository.CourseClassRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class CourseClassStatusScheduler {

    private final CourseClassRepository courseClassRepository;

    public CourseClassStatusScheduler(CourseClassRepository courseClassRepository) {
        this.courseClassRepository = courseClassRepository;
    }

    // 수강 시작일이 지난 강의 자동 종료
    @Scheduled(cron = "0 5 0 * * *") // 매일 00:05 실행
    @Transactional
    public void closeExpiredOpenCourseClasses() {
        List<CourseClass> openCourseClasses = courseClassRepository.findByStatusAndStartDateBefore(
                CourseClassStatus.OPEN,
                LocalDate.now()
        );

        for (CourseClass courseClass : openCourseClasses) {
            courseClass.changeStatus(CourseClassStatus.CLOSED);
        }
    }
}
