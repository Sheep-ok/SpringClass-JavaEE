package com.example.attendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class StudentController
{

    @GetMapping("/student/info")
    public String GetStudentInfo() {
        return "姓名:杨茜，学号42411114，班级：人工智能";
    }

    @PostMapping("/student/attendance")
    public String StudentAttendance(@RequestBody String StudentId)
    {
        return "学号为 " + StudentId + " 的学生打卡成功!";
    }

    @GetMapping("/student/courses")
    public List<String> GetCourses()
    {
        return Arrays.asList("机器学习与数据挖掘","数据库原理与应用");
    }
}
