package com.example.attendance.repository;

import com.example.attendance.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseId(String courseId);

    List<Course> findByTeacherId(String teacherId);

    List<Course> findByClassName(String className);

    List<Course> findByTeacherIdOrClassName(String teacherId, String className);

    List<Course> findByWeekDay(Integer weekDay);
}