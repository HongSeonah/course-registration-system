package com.example.courseregistration.domain.courseclass.repository;

import com.example.courseregistration.domain.courseclass.entity.CourseClass;
import com.example.courseregistration.domain.courseclass.entity.CourseClassStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseClassRepository extends JpaRepository<CourseClass, Long> {

    List<CourseClass> findByStatus(CourseClassStatus status);

    // 강의 조회 및 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CourseClass c WHERE c.id = :courseClassId")
    Optional<CourseClass> findByIdForUpdate(@Param("courseClassId") Long courseClassId);
}
