package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.util.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * 分页+排序查询所有考勤
     * @param page 页码（从0开始）
     * @param size 每页条数
     * @param sortField 排序字段（如 checkInTime）
     * @param sortDirection 排序方向（asc/desc）
     */
    /*
    @GetMapping("/page")
    public Result<Page<Attendance>> getAttendancePage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Attendance> attendancePage = attendanceService.getAttendancePage(pageRequest);
        return Result.success(attendancePage);
    }
    */

    /**
     * 按学号分页+排序查询
     */
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

        // 只有 ADMIN 和 TEACHER 能查看所有考勤
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
        @GetMapping("/page")
        public Result<Page<Attendance>> getAttendancePage(Pageable pageable) {
            // 你的分页查询代码
            return null;
        }

        // 所有角色都能查看自己的考勤
        @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'USER')")
        @GetMapping("/student/{studentId}")
        public Result<Page<Attendance>> getStudentAttendance(
                @PathVariable String studentId, Pageable pageable) {
            // 你的学生考勤查询代码
            return null;
        }


}