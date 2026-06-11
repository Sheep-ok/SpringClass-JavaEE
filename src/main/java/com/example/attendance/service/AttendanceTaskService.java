package com.example.attendance.service;

import com.example.attendance.entity.AttendanceTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceTaskService {
    
    /**
     * 创建考勤任务
     */
    AttendanceTask createTask(AttendanceTask task);
    
    /**
     * 根据ID查询考勤任务
     */
    AttendanceTask getTaskById(Long id);
    
    /**
     * 根据考勤码查询
     */
    AttendanceTask getTaskByCode(String taskCode);
    
    /**
     * 获取教师的考勤任务列表
     */
    List<AttendanceTask> getTasksByTeacherId(String teacherId);
    
    /**
     * 获取课程的考勤任务列表
     */
    List<AttendanceTask> getTasksByCourseId(String courseId);
    
    /**
     * 获取学生可参与的考勤任务列表
     */
    List<AttendanceTask> getAvailableTasksForStudent(List<String> courseIds);
    
    /**
     * 更新考勤任务状态
     */
    void updateTaskStatus(Long taskId, Integer status);
    
    /**
     * 结束考勤任务
     */
    void endTask(Long taskId);
    
    /**
     * 取消考勤任务
     */
    void cancelTask(Long taskId);
    
    /**
     * 自动结束过期的考勤任务
     */
    void autoEndExpiredTasks();
    
    /**
     * 验证考勤码是否有效
     */
    boolean validateTaskCode(String taskCode, String courseId);
    
    /**
     * 获取考勤任务详情（包含统计信息）
     */
    AttendanceTask getTaskDetail(Long taskId);
    
    /**
     * 分页查询教师的考勤任务
     */
    Page<AttendanceTask> getTasksByTeacherId(String teacherId, Pageable pageable);
}
