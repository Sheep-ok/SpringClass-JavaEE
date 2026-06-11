package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {
    Page<Attendance> findByStudentId(String studentId, Pageable pageable);

    List<Attendance> findByStudentId(String studentId);

    List<Attendance> findByCourseId(String courseId);

    List<Attendance> findByTaskId(Long taskId);

    Attendance findByStudentIdAndTaskId(String studentId, Long taskId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :start AND :end " +
           "AND (:courseId IS NULL OR a.courseId = :courseId) " +
           "AND (:className IS NULL OR :className = '' OR EXISTS (SELECT s FROM Student s WHERE s.studentId = a.studentId AND s.className = :className))")
    long countByTimeRange(@Param("start") LocalDateTime start, 
                          @Param("end") LocalDateTime end, 
                          @Param("courseId") String courseId, 
                          @Param("className") String className);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentStatus = :status " +
           "AND a.checkInTime BETWEEN :start AND :end " +
           "AND (:courseId IS NULL OR a.courseId = :courseId) " +
           "AND (:className IS NULL OR :className = '' OR EXISTS (SELECT s FROM Student s WHERE s.studentId = a.studentId AND s.className = :className))")
    long countByStatusAndTimeRange(@Param("status") int status,
                                   @Param("start") LocalDateTime start, 
                                   @Param("end") LocalDateTime end, 
                                   @Param("courseId") String courseId, 
                                   @Param("className") String className);

    @Query("SELECT a.courseId, c.courseName, COUNT(a) FROM Attendance a " +
           "LEFT JOIN Course c ON a.courseId = c.courseId " +
           "WHERE a.checkInTime BETWEEN :start AND :end " +
           "AND (:className IS NULL OR :className = '' OR EXISTS (SELECT s FROM Student s WHERE s.studentId = a.studentId AND s.className = :className)) " +
           "GROUP BY a.courseId, c.courseName ORDER BY COUNT(a) DESC")
    List<Object[]> getCourseStats(@Param("start") LocalDateTime start, 
                                   @Param("end") LocalDateTime end, 
                                   @Param("className") String className);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :start AND :end " +
           "AND a.courseId IN :courseIds " +
           "AND (:className IS NULL OR :className = '' OR EXISTS (SELECT s FROM Student s WHERE s.studentId = a.studentId AND s.className = :className))")
    long countByTimeRangeAndCourseIds(@Param("start") LocalDateTime start, 
                                       @Param("end") LocalDateTime end, 
                                       @Param("courseIds") List<String> courseIds, 
                                       @Param("className") String className);

    // 教师专用：统计选了教师课程的学生的考勤记录
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.checkInTime BETWEEN :start AND :end " +
           "AND a.courseId IN :courseIds " +
           "AND EXISTS (SELECT cs FROM CourseSelection cs WHERE cs.studentId = a.studentId AND cs.courseId = a.courseId AND cs.status = 1) " +
           "AND (:className IS NULL OR :className = '' OR EXISTS (SELECT s FROM Student s WHERE s.studentId = a.studentId AND s.className = :className))")
    long countByTimeRangeForTeacherCourses(@Param("start") LocalDateTime start, 
                                            @Param("end") LocalDateTime end, 
                                            @Param("courseIds") List<String> courseIds, 
                                            @Param("className") String className);

    // 教师专用：统计选了教师课程的学生的特定状态考勤记录
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentStatus = :status " +
           "AND a.checkInTime BETWEEN :start AND :end " +
           "AND a.courseId IN :courseIds " +
           "AND EXISTS (SELECT cs FROM CourseSelection cs WHERE cs.studentId = a.studentId AND cs.courseId = a.courseId AND cs.status = 1) " +
           "AND (:className IS NULL OR :className = '' OR EXISTS (SELECT s FROM Student s WHERE s.studentId = a.studentId AND s.className = :className))")
    long countByStatusForTeacherCourses(@Param("status") int status,
                                         @Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end, 
                                         @Param("courseIds") List<String> courseIds, 
                                         @Param("className") String className);
}