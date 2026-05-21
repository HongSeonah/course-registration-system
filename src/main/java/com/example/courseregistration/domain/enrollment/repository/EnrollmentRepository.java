package com.example.courseregistration.domain.enrollment.repository;

import com.example.courseregistration.domain.enrollment.entity.EnrollmentStatus;
import com.example.courseregistration.domain.enrollment.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    long countByCourseClassIdAndStatusIn(Long courseClassId, Collection<EnrollmentStatus> statuses);
}
