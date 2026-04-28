package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    // 1. 根据学号查询学生（方法名规则自动生成SQL）
    Optional<Student> findByStudentId(String studentId);

    // 2. 根据班级查询学生列表
    List<Student> findByClassName(String className);

    // 3. 统计某班级学生数量（自定义查询）
    long countByClassName(String className);


}