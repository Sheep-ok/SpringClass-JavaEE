package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AttendanceService {
    Page<Attendance> getAttendancePage(Pageable pageable);
    Page<Attendance> getAttendancePageByStudentId(String studentId, Pageable pageable);
    Page<Attendance> getAttendancePageByConditions(String studentId, String courseId, Integer studentStatus, Pageable pageable);

    String checkIn(String studentId, String studentName, String courseId, String signInId, String ipAddress);
    
    String checkIn(String studentId, String studentName, String courseId, Integer studentStatus, String signInId, String ipAddress);
    
    String checkIn(String studentId, String studentName, String courseId, Integer studentStatus, String signInId, String ipAddress, String reason);
    
    String batchCheckIn(String courseId, String[] studentIds, Integer studentStatus, String signInId, String ipAddress);
    
    String updateAttendance(Long id, Integer studentStatus);
    
    String deleteAttendance(Long id);
    
    Attendance getAttendanceById(Long id);
    }