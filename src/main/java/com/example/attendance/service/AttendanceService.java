package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import java.util.List;

public interface AttendanceService {

    // 新增考勤
    String addAttendance(Attendance attendance);

    // 根据ID查询考勤
    Attendance getAttendanceById(Long id);

    // 根据学号查询考勤
    List<Attendance> getByStudentId(String studentId);

    // 根据课程ID查询考勤
    List<Attendance> getByCourseId(String courseId);

    // 查询所有考勤
    List<Attendance> getAllAttendance();

    // 更新考勤
    String updateAttendance(Attendance attendance);

    // 删除考勤
    String deleteAttendance(Long id);
}