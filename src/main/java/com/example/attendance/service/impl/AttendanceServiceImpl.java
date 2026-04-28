package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public Page<Attendance> getAttendancePage(Pageable pageable) {
        return attendanceRepository.findAll(pageable);
    }

    @Override
    public Page<Attendance> getAttendancePageByStudentId(String studentId, Pageable pageable) {
        return attendanceRepository.findByStudentId(studentId, pageable);
    }

    // AttendanceServiceImpl 实现
    @Override
    public Page<Attendance> getAttendancePageByConditions(String studentId, String courseId, Integer studentStatus, Pageable pageable) {
        return attendanceRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 学号条件
            if (studentId != null && !studentId.isBlank()) {
                predicates.add(cb.equal(root.get("studentId"), studentId));
            }

            // 课程ID条件
            if (courseId != null && !courseId.isBlank()) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }

            // 状态条件
            if (studentStatus != null) {
                predicates.add(cb.equal(root.get("studentStatus"), studentStatus));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }
}