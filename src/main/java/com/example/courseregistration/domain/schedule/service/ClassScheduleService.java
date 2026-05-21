package com.example.courseregistration.domain.schedule.service;

import com.example.courseregistration.domain.courseclass.entity.CourseClass;
import com.example.courseregistration.domain.courseclass.exception.CourseClassErrorCode;
import com.example.courseregistration.domain.courseclass.repository.CourseClassRepository;
import com.example.courseregistration.domain.schedule.dto.request.ClassScheduleCreateRequest;
import com.example.courseregistration.domain.schedule.dto.request.ClassScheduleUpdateRequest;
import com.example.courseregistration.domain.schedule.dto.response.ClassScheduleResponse;
import com.example.courseregistration.domain.schedule.entity.ClassSchedule;
import com.example.courseregistration.domain.schedule.exception.ScheduleErrorCode;
import com.example.courseregistration.domain.schedule.repository.ClassScheduleRepository;
import com.example.courseregistration.global.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClassScheduleService {

    private final ClassScheduleRepository scheduleRepository;
    private final CourseClassRepository courseClassRepository;

    public ClassScheduleService(ClassScheduleRepository scheduleRepository,
                                 CourseClassRepository courseClassRepository) {
        this.scheduleRepository = scheduleRepository;
        this.courseClassRepository = courseClassRepository;
    }

    // 시간표 추가
    @Transactional
    public ClassScheduleResponse create(Long courseClassId, ClassScheduleCreateRequest request) {
        validateTimeRange(request.startTime(), request.endTime());
        CourseClass courseClass = getCourseClass(courseClassId);

        ClassSchedule schedule = ClassSchedule.builder()
                .courseClass(courseClass)
                .dayOfWeek(request.dayOfWeek())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .room(request.room())
                .build();

        return ClassScheduleResponse.from(scheduleRepository.save(schedule));
    }

    // 시간표 목록 조회
    public List<ClassScheduleResponse> findAllByCourseClassId(Long courseClassId) {
        getCourseClass(courseClassId); // 강의 존재 확인
        return scheduleRepository.findByCourseClassId(courseClassId).stream()
                .map(ClassScheduleResponse::from)
                .toList();
    }

    // 시간표 수정
    @Transactional
    public ClassScheduleResponse update(Long scheduleId, ClassScheduleUpdateRequest request) {
        validateTimeRange(request.startTime(), request.endTime());
        ClassSchedule schedule = getSchedule(scheduleId);

        schedule.update(
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                request.room()
        );

        return ClassScheduleResponse.from(schedule);
    }

    // 시간표 삭제
    @Transactional
    public void delete(Long scheduleId) {
        ClassSchedule schedule = getSchedule(scheduleId);
        scheduleRepository.delete(schedule);
    }

    // 시간표 조회
    private ClassSchedule getSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BaseException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
    }

    // 강의 조회
    private CourseClass getCourseClass(Long courseClassId) {
        return courseClassRepository.findById(courseClassId)
                .orElseThrow(() -> new BaseException(CourseClassErrorCode.COURSE_CLASS_NOT_FOUND));
    }

    // 시간 범위 검증
    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (endTime.isBefore(startTime)) {
            throw new BaseException(ScheduleErrorCode.INVALID_TIME_RANGE);
        }
    }
}
