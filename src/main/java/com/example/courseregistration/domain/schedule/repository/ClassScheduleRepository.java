package com.example.courseregistration.domain.schedule.repository;

import com.example.courseregistration.domain.schedule.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByCourseClassId(Long courseClassId);

    // 중복 시간 확인
    // TODO: startDate와 endDate 안에서 겹치는지 검증해야함
    @Query("SELECT cs FROM ClassSchedule cs " +
           "WHERE cs.courseClass.id = :courseClassId " +
           "AND cs.dayOfWeek = :dayOfWeek " +
           "AND cs.startTime < :endTime " +
           "AND cs.endTime > :startTime")
    List<ClassSchedule> findOverlappingSchedules(
            @Param("courseClassId") Long courseClassId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}
