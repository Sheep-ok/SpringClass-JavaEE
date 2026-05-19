package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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

    @Override
    public Page<Attendance> getAttendancePageByConditions(String studentId, String courseId, Integer studentStatus, Pageable pageable) {
        return attendanceRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (studentId != null && !studentId.isBlank()) {
                predicates.add(cb.equal(root.get("studentId"), studentId));
            }

            if (courseId != null && !courseId.isBlank()) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }

            if (studentStatus != null) {
                predicates.add(cb.equal(root.get("studentStatus"), studentStatus));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
    }

    @Override
    public String checkIn(String studentId, String studentName, String courseId, String signInId, String ipAddress) {
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setStudentName(studentName);
        attendance.setCourseId(courseId);
        attendance.setSignInId(signInId);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStudentStatus(1);
        attendance.setIpAddress(ipAddress);
        attendance.setCreateTime(LocalDateTime.now());

        attendanceRepository.save(attendance);
        return "签到成功";
    }

    @Override
    public String checkIn(String studentId, String studentName, String courseId, Integer studentStatus, String signInId, String ipAddress) {
        return checkIn(studentId, studentName, courseId, studentStatus, signInId, ipAddress, null);
    }

    @Override
    public String checkIn(String studentId, String studentName, String courseId, Integer studentStatus, String signInId, String ipAddress, String reason) {
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setStudentName(studentName);
        attendance.setCourseId(courseId);
        attendance.setSignInId(signInId);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStudentStatus(studentStatus != null ? studentStatus : 1);
        attendance.setIpAddress(ipAddress);
        attendance.setCreateTime(LocalDateTime.now());
        attendance.setReason(reason);

        attendanceRepository.save(attendance);
        
        String statusMsg = switch (studentStatus != null ? studentStatus : 1) {
            case 1 -> "签到成功";
            case 2 -> "迟到登记成功";
            case 3 -> "早退登记成功";
            case 4 -> "请假登记成功";
            default -> "考勤登记成功";
        };
        return statusMsg;
    }

    @Override
    public String batchCheckIn(String courseId, String[] studentIds, Integer studentStatus, String signInId, String ipAddress) {
        String batchSignInId = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime now = LocalDateTime.now();
        
        List<Attendance> attendanceList = new ArrayList<>();
        for (String studentId : studentIds) {
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setStudentName("");
            attendance.setCourseId(courseId);
            attendance.setSignInId(batchSignInId);
            attendance.setCheckInTime(now);
            attendance.setStudentStatus(studentStatus != null ? studentStatus : 1);
            attendance.setIpAddress(ipAddress);
            attendance.setCreateTime(now);
            attendanceList.add(attendance);
        }
        
        attendanceRepository.saveAll(attendanceList);
        
        String statusMsg = switch (studentStatus != null ? studentStatus : 1) {
            case 1 -> "批量签到成功";
            case 2 -> "批量缺勤登记成功";
            case 3 -> "批量早退登记成功";
            case 4 -> "批量请假登记成功";
            default -> "批量考勤登记成功";
        };
        return statusMsg;
    }

    @Override
    public String updateAttendance(Long id, Integer studentStatus) {
        return attendanceRepository.findById(id)
            .map(attendance -> {
                attendance.setStudentStatus(studentStatus);
                attendance.setUpdateTime(LocalDateTime.now());
                attendanceRepository.save(attendance);
                
                String statusMsg = switch (studentStatus) {
                    case 1 -> "已修改为签到";
                    case 2 -> "已修改为迟到";
                    case 3 -> "已修改为早退";
                    case 4 -> "已修改为请假";
                    default -> "考勤状态已更新";
                };
                return statusMsg;
            })
            .orElse("考勤记录不存在");
    }

    @Override
    public String deleteAttendance(Long id) {
        if (attendanceRepository.existsById(id)) {
            attendanceRepository.deleteById(id);
            return "删除成功";
        }
        return "考勤记录不存在";
    }

    @Override
    public Attendance getAttendanceById(Long id) {
        return attendanceRepository.findById(id).orElse(null);
    }
}