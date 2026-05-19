package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/page/student/{studentId}")
    public Result<Page<Attendance>> getAttendancePageByStudentId(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Attendance> pageResult = attendanceService.getAttendancePageByStudentId(studentId, pageRequest);
        return Result.success(pageResult);
    }

    @GetMapping("/search")
    public Result<Page<Attendance>> searchAttendance(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) Integer studentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Attendance> result = attendanceService.getAttendancePageByConditions(studentId, courseId, studentStatus, pageRequest);
        return Result.success(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/page")
    public Result<Page<Attendance>> getAttendancePage(Pageable pageable) {
        Page<Attendance> attendancePage = attendanceService.getAttendancePage(pageable);
        return Result.success(attendancePage);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/all")
    public Result<Page<Attendance>> getAllAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkInTime"));
        Page<Attendance> attendancePage = attendanceService.getAttendancePage(pageRequest);
        return Result.success(attendancePage);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'USER')")
    @GetMapping("/student/{studentId}")
    public Result<Page<Attendance>> getStudentAttendance(
            @PathVariable String studentId, Pageable pageable) {
        Page<Attendance> attendancePage = attendanceService.getAttendancePageByStudentId(studentId, pageable);
        return Result.success(attendancePage);
    }

    @PostMapping("/checkin")
    public Result<String> checkIn(
            @RequestParam String studentId,
            @RequestParam String studentName,
            @RequestParam String courseId,
            HttpServletRequest request) {
        
        String signInId = UUID.randomUUID().toString().substring(0, 8);
        String ipAddress = getClientIpAddress(request);
        
        String result = attendanceService.checkIn(studentId, studentName, courseId, signInId, ipAddress);
        return Result.success(result);
    }

    @PostMapping("/checkin/status")
    public Result<String> checkInWithStatus(
            @RequestParam String studentId,
            @RequestParam String studentName,
            @RequestParam String courseId,
            @RequestParam(defaultValue = "1") Integer studentStatus,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        
        String signInId = UUID.randomUUID().toString().substring(0, 8);
        String ipAddress = getClientIpAddress(request);
        
        String result = attendanceService.checkIn(studentId, studentName, courseId, studentStatus, signInId, ipAddress, reason);
        return Result.success(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/batch")
    public Result<String> batchCheckIn(
            @RequestParam String courseId,
            @RequestParam String studentIds,
            @RequestParam(defaultValue = "1") Integer studentStatus,
            HttpServletRequest request) {
        
        String signInId = UUID.randomUUID().toString().substring(0, 8);
        String ipAddress = getClientIpAddress(request);
        String[] studentIdArray = studentIds.split(",");
        
        String result = attendanceService.batchCheckIn(courseId, studentIdArray, studentStatus, signInId, ipAddress);
        return Result.success(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/{id}")
    public Result<Attendance> getAttendanceById(@PathVariable Long id) {
        Attendance attendance = attendanceService.getAttendanceById(id);
        if (attendance != null) {
            return Result.success(attendance);
        }
        return Result.error("考勤记录不存在");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PutMapping("/{id}")
    public Result<String> updateAttendance(
            @PathVariable Long id,
            @RequestParam Integer studentStatus) {
        String result = attendanceService.updateAttendance(id, studentStatus);
        return Result.success(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @DeleteMapping("/{id}")
    public Result<String> deleteAttendance(@PathVariable Long id) {
        String result = attendanceService.deleteAttendance(id);
        return Result.success(result);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}