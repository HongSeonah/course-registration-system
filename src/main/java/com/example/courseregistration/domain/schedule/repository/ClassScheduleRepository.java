package com.example.courseregistration.domain.schedule.repository;

import com.example.courseregistration.domain.schedule.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, Long> {
    List<ClassSchedule> findByCourseClassId(Long courseClassId);
}
