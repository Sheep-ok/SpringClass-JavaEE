package com.example.attendance.service;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {

    // 创建学生
    String createStudent(Student student);

    // 根据ID查询学生
    Student getStudentById(String id);

    // 查询全部学生
    List<Student> getAllStudents();

    // 根据班级查询
    List<Student> getStudentsByClassName(String className);

    // 更新学生信息
    String updateStudent(Student student);

    // 删除学生
    String deleteStudent(String studentId);

    // 分页查询所有学生
    Page<Student> getStudentPage(Pageable pageable);

    // 根据班级分页查询
    Page<Student> getStudentPageByClassName(String className, Pageable pageable);
}