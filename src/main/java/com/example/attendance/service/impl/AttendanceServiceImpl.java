package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public String addAttendance(Attendance attendance) {
        // 自动设置创建时间
        attendance.setCreateTime(LocalDateTime.now());
        attendanceRepository.save(attendance);
        return "考勤记录添加成功";
    }

    @Override
    public Attendance getAttendanceById(Long id) {
        return attendanceRepository.findById(id).orElse(null);
    }

    @Override
    public List<Attendance> getByStudentId(String studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }

    @Override
    public List<Attendance> getByCourseId(String courseId) {
        return attendanceRepository.findByCourseId(courseId);
    }

    @Override
    public List<Attendance> getAllAttendance() {
        return attendanceRepository.findAll();
    }

    @Override
    public String updateAttendance(Attendance attendance) {
        attendanceRepository.save(attendance);
        return "考勤记录更新成功";
    }

    @Override
    public String deleteAttendance(Long id) {
        attendanceRepository.deleteById(id);
        return "考勤记录删除成功";
    }
}