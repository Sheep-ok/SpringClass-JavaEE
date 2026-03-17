package com.example.attendance.controller;

import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Result;
import com.example.attendance.entity.Student;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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


    // 任务一：路径参数查询单个学生
    @GetMapping("/student/info/{id}")
    public Result<Student> getStudentById(@PathVariable String id) {
        Student student = new Student(id, "张三", "计算机", 20);
        return Result.success(student);
    }

    // 任务二：查询参数查询学生列表
    @GetMapping("/student/list")
    public Result<List<Student>> searchStudent(
            @RequestParam String className,
            @RequestParam(defaultValue = "1") int page) {

        List<Student> students = new ArrayList<>();
        students.add(new Student("001", "张三", className, 20));
        students.add(new Student("002", "李四", className, 19));
        students.add(new Student("003", "王五", className, 20));

        return Result.success(students);
    }

    // 任务三：JSON 体参数更新考勤记录
    @PostMapping("/attendance/update")
    public Result<String> updateAttendance(@RequestBody AttendanceRecord record) {
        String result = String.format("更新学生[%s]在[%s]的考勤状态为：%s",
                record.getStudentId(), record.getDate(), record.getStatus());
        return Result.success(result);
    }
}
