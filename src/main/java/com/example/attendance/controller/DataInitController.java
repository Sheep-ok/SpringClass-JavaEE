package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.util.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/data")
public class DataInitController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public DataInitController(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @PostMapping("/init/gender")
    public Result<String> initGender() {
        int userCount = 0;
        int studentCount = 0;

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getGender() == null || user.getGender().isEmpty()) {
                user.setGender(Math.random() > 0.5 ? "男" : "女");
                userRepository.save(user);
                userCount++;
            }
        }

        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            if (student.getGender() == null || student.getGender().isEmpty()) {
                student.setGender(Math.random() > 0.5 ? "男" : "女");
                studentRepository.save(student);
                studentCount++;
            }
        }

        return Result.success("性别初始化完成！用户表更新 " + userCount + " 条，学生表更新 " + studentCount + " 条");
    }

    @PostMapping("/sync/students")
    public Result<String> syncStudentsFromUsers() {
        int createdCount = 0;
        int updatedCount = 0;

        List<User> users = userRepository.findAll();
        for (User user : users) {
            if ("STUDENT".equals(user.getUserrole()) || "USER".equals(user.getUserrole())) {
                String studentId = user.getUsername();
                Student existingStudent = studentRepository.findByStudentId(studentId).orElse(null);
                
                if (existingStudent == null) {
                    Student student = new Student();
                    student.setStudentId(studentId);
                    student.setName(user.getRealName() != null && !user.getRealName().isEmpty() ? user.getRealName() : user.getUsername());
                    student.setGender(user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : (Math.random() > 0.5 ? "男" : "女"));
                    student.setClassName("未分配");
                    student.setAge(20);
                    studentRepository.save(student);
                    createdCount++;
                } else {
                    boolean updated = false;
                    String newName = user.getRealName() != null && !user.getRealName().isEmpty() ? user.getRealName() : user.getUsername();
                    if (!newName.equals(existingStudent.getName())) {
                        existingStudent.setName(newName);
                        updated = true;
                    }
                    String newGender = user.getGender() != null && !user.getGender().isEmpty() ? user.getGender() : existingStudent.getGender();
                    if (!newGender.equals(existingStudent.getGender())) {
                        existingStudent.setGender(newGender);
                        updated = true;
                    }
                    if (updated) {
                        studentRepository.save(existingStudent);
                        updatedCount++;
                    }
                }
            }
        }

        return Result.success("学生数据同步完成！新增 " + createdCount + " 条，更新 " + updatedCount + " 条");
    }
}