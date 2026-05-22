package com.example.courseregistration.domain.enrollment.repository;

import com.example.courseregistration.domain.enrollment.entity.Enrollment;
import com.example.courseregistration.domain.enrollment.entity.EnrollmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    long countByCourseClassIdAndStatusIn(Long courseClassId, Collection<EnrollmentStatus> statuses);

    // 특정 사용자의 특정 강의 신청 조회
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.courseClass.id = :courseClassId " +
           "AND e.classmate.id = :classmateId " +
           "AND e.status IN :statuses")
    Optional<Enrollment> findByCourseClassAndClassmate(
            @Param("courseClassId") Long courseClassId,
            @Param("classmateId") Long classmateId,
            @Param("statuses") Collection<EnrollmentStatus> statuses);

    // 특정 강의의 모든 신청 조회
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.courseClass.id = :courseClassId " +
           "ORDER BY e.id")
    List<Enrollment> findByCourseClassId(@Param("courseClassId") Long courseClassId);

    // 특정 사용자의 모든 신청 조회
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.classmate.id = :classmateId " +
           "ORDER BY e.appliedAt DESC")
    List<Enrollment> findByClassmateId(@Param("classmateId") Long classmateId);

    // 특정 사용자의 신청 내역 페이지 조회
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.classmate.id = :classmateId")
    org.springframework.data.domain.Page<Enrollment> findByClassmateId(@Param("classmateId") Long classmateId, Pageable pageable);

    // 시간표 충돌 확인 (이미 신청한 강의 스케줄 조회)
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.classmate.id = :classmateId " +
           "AND e.status IN :statuses " +
           "AND e.courseClass.id != :excludeCourseClassId")
    List<Enrollment> findConflictingEnrollments(
            @Param("classmateId") Long classmateId,
            @Param("statuses") Collection<EnrollmentStatus> statuses,
            @Param("excludeCourseClassId") Long excludeCourseClassId);

    // 대기열 1순위 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.courseClass.id = :courseClassId " +
           "AND e.status = :status " +
           "ORDER BY e.waitlistOrder ASC")
    Optional<Enrollment> findFirstWaitlistEnrollmentForUpdate(
            @Param("courseClassId") Long courseClassId,
            @Param("status") EnrollmentStatus status);

    // 대기열 순번 재정렬
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.courseClass.id = :courseClassId " +
           "AND e.status = :status " +
           "ORDER BY e.waitlistOrder ASC")
    List<Enrollment> findWaitlistEnrollmentsForUpdate(
            @Param("courseClassId") Long courseClassId,
            @Param("status") EnrollmentStatus status);
}
