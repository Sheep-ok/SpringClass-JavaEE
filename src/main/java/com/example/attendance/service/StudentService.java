package com.example.attendance.service;

import com.example.attendance.entity.Student;
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
}