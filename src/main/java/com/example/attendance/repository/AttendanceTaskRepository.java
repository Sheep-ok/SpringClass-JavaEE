package com.example.attendance.repository;

import com.example.attendance.entity.AttendanceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceTaskRepository extends JpaRepository<AttendanceTask, Long> {
    
    // 根据考勤码查询
    Optional<AttendanceTask> findByTaskCode(String taskCode);
    
    // 根据课程ID查询
    List<AttendanceTask> findByCourseId(String courseId);
    
    // 根据教师ID查询
    List<AttendanceTask> findByTeacherId(String teacherId);
    
    // 根据状态查询
    List<AttendanceTask> findByStatus(Integer status);
    
    // 查询指定课程当前进行中的考勤任务
    @Query("SELECT t FROM AttendanceTask t WHERE t.courseId = :courseId AND t.status = 1 AND t.endTime > :now")
    List<AttendanceTask> findActiveTasksByCourseId(@Param("courseId") String courseId, @Param("now") LocalDateTime now);
    
    // 查询教师的所有考勤任务（按创建时间倒序）
    @Query("SELECT t FROM AttendanceTask t WHERE t.teacherId = :teacherId ORDER BY t.createTime DESC")
    List<AttendanceTask> findByTeacherIdOrderByCreateTimeDesc(@Param("teacherId") String teacherId);
    
    // 查询需要自动结束的考勤任务（宽限结束时间已过但状态仍为进行中）
    @Query("SELECT t FROM AttendanceTask t WHERE t.status = 1 AND t.worseTime < :now")
    List<AttendanceTask> findExpiredTasks(@Param("now") LocalDateTime now);
    
    // 查询学生可参与的考勤任务（通过课程ID和当前时间，包含结束后的10分钟宽限期）
    @Query("SELECT t FROM AttendanceTask t WHERE t.courseId IN :courseIds AND t.status = 1 AND t.startTime <= :now AND t.endTime > :tenMinutesAgo ORDER BY t.createTime DESC")
    List<AttendanceTask> findAvailableTasksForStudent(@Param("courseIds") List<String> courseIds, @Param("now") LocalDateTime now, @Param("tenMinutesAgo") LocalDateTime tenMinutesAgo);
}
