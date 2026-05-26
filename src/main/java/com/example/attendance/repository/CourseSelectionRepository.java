package com.example.attendance.repository;

import com.example.attendance.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseSelectionRepository extends JpaRepository<CourseSelection, Long> {
    List<CourseSelection> findByStudentId(String studentId);

    List<CourseSelection> findByStudentIdAndStatus(String studentId, Integer status);

    List<CourseSelection> findByCourseId(String courseId);

    List<CourseSelection> findByClassName(String className);

    List<CourseSelection> findByCourseIdAndStatus(String courseId, Integer status);

    boolean existsByStudentIdAndCourseId(String studentId, String courseId);
    
    long countDistinctStudentIdByCourseIdIn(List<String> courseIds);
    
    @Query("SELECT COUNT(DISTINCT cs.studentId) FROM CourseSelection cs WHERE cs.courseId IN :courseIds AND cs.status = 1")
    long countDistinctStudentIdByCourseIdInAndStatus(@Param("courseIds") List<String> courseIds);
    
    @Query("SELECT COUNT(DISTINCT cs.studentId) FROM CourseSelection cs JOIN Student s ON cs.studentId = s.studentId WHERE cs.courseId IN :courseIds AND s.className = :className AND cs.status = 1")
    long countDistinctStudentIdByCourseIdsAndClassName(@Param("courseIds") List<String> courseIds, @Param("className") String className);
    
    @Query("SELECT DISTINCT s.className FROM CourseSelection cs JOIN Student s ON cs.studentId = s.studentId WHERE cs.courseId IN :courseIds")
    List<String> findDistinctClassNamesByCourseIds(@Param("courseIds") List<String> courseIds);
}