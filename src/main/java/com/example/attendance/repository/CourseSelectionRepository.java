package com.example.attendance.repository;

import com.example.attendance.entity.CourseSelection;
import org.springframework.data.jpa.repository.JpaRepository;
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
}