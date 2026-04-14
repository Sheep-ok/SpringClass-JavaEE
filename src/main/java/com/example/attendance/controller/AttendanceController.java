package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.util.Result;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // 新增考勤
    @PostMapping("/add")
    public Result<String> addAttendance(@RequestBody Attendance attendance) {
        return Result.success(attendanceService.addAttendance(attendance));
    }

    // 根据ID查询考勤
    @GetMapping("/{id}")
    public Result<Attendance> getAttendanceById(@PathVariable Long id) {
        return Result.success(attendanceService.getAttendanceById(id));
    }

    // 根据学号查询考勤
    @GetMapping("/student/{studentId}")
    public Result<List<Attendance>> getByStudentId(@PathVariable String studentId) {
        return Result.success(attendanceService.getByStudentId(studentId));
    }

    // 根据课程ID查询考勤
    @GetMapping("/course/{courseId}")
    public Result<List<Attendance>> getByCourseId(@PathVariable String courseId) {
        return Result.success(attendanceService.getByCourseId(courseId));
    }

    // 查询所有考勤
    @GetMapping("/list")
    public Result<List<Attendance>> getAllAttendance() {
        return Result.success(attendanceService.getAllAttendance());
    }

    // 更新考勤
    @PutMapping("/update")
    public Result<String> updateAttendance(@RequestBody Attendance attendance) {
        return Result.success(attendanceService.updateAttendance(attendance));
    }

    // 删除考勤
    @DeleteMapping("/{id}")
    public Result<String> deleteAttendance(@PathVariable Long id) {
        return Result.success(attendanceService.deleteAttendance(id));
    }
}