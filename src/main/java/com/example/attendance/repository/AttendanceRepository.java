package com.example.attendance.repository;

import com.example.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // 根据学号查询所有考勤
    List<Attendance> findByStudentId(String studentId);

    // 根据课程ID查询考勤
    List<Attendance> findByCourseId(String courseId);

    // 根据签到ID查询单条记录
    Attendance findBySignInId(String signInId);

    // 根据学生状态查询
    List<Attendance> findByStudentStatus(Integer status);
}