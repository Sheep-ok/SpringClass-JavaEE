package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceService {
    Page<Attendance> getAttendancePage(Pageable pageable);
    Page<Attendance> getAttendancePageByStudentId(String studentId, Pageable pageable);
    // AttendanceService 接口
    Page<Attendance> getAttendancePageByConditions(String studentId, String courseId, Integer studentStatus, Pageable pageable);

}