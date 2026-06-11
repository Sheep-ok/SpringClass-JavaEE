package com.example.attendance.service.impl;

import com.example.attendance.entity.AttendanceTask;
import com.example.attendance.repository.AttendanceTaskRepository;
import com.example.attendance.service.AttendanceTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AttendanceTaskServiceImpl implements AttendanceTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(AttendanceTaskServiceImpl.class);
    private final AttendanceTaskRepository taskRepository;
    
    public AttendanceTaskServiceImpl(AttendanceTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }
    
    @Override
    @Transactional
    public AttendanceTask createTask(AttendanceTask task) {
        // 生成六位随机考勤码
        String taskCode = generateTaskCode();
        task.setTaskCode(taskCode);
        
        // 验证时间
        if (task.getStartTime() == null || task.getEndTime() == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为空");
        }
        if (task.getEndTime().isBefore(task.getStartTime())) {
            throw new IllegalArgumentException("结束时间不能早于开始时间");
        }
        
        AttendanceTask savedTask = taskRepository.save(task);
        logger.info("创建考勤任务成功，任务ID: {}, 考勤码: {}", savedTask.getId(), savedTask.getTaskCode());
        return savedTask;
    }
    
    @Override
    public AttendanceTask getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }
    
    @Override
    public AttendanceTask getTaskByCode(String taskCode) {
        return taskRepository.findByTaskCode(taskCode).orElse(null);
    }
    
    @Override
    public List<AttendanceTask> getTasksByTeacherId(String teacherId) {
        return taskRepository.findByTeacherIdOrderByCreateTimeDesc(teacherId);
    }
    
    @Override
    public List<AttendanceTask> getTasksByCourseId(String courseId) {
        return taskRepository.findByCourseId(courseId);
    }
    
    @Override
    public List<AttendanceTask> getAvailableTasksForStudent(List<String> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesAgo = now.minusMinutes(10);
        return taskRepository.findAvailableTasksForStudent(courseIds, now, tenMinutesAgo);
    }
    
    @Override
    @Transactional
    public void updateTaskStatus(Long taskId, Integer status) {
        Optional<AttendanceTask> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            AttendanceTask task = taskOpt.get();
            task.setStatus(status);
            taskRepository.save(task);
            logger.info("更新考勤任务状态，任务ID: {}, 新状态: {}", taskId, status);
        }
    }
    
    @Override
    @Transactional
    public void endTask(Long taskId) {
        updateTaskStatus(taskId, 2); // 2-已结束
    }
    
    @Override
    @Transactional
    public void cancelTask(Long taskId) {
        updateTaskStatus(taskId, 3); // 3-已取消
    }
    
    @Override
    @Transactional
    public void autoEndExpiredTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<AttendanceTask> expiredTasks = taskRepository.findExpiredTasks(now);
        for (AttendanceTask task : expiredTasks) {
            task.setStatus(2); // 设置为已结束
            taskRepository.save(task);
            logger.info("自动结束考勤任务，任务ID: {}, 考勤码: {}", task.getId(), task.getTaskCode());
        }
    }
    
    @Override
    public boolean validateTaskCode(String taskCode, String courseId) {
        AttendanceTask task = getTaskByCode(taskCode);
        if (task == null) {
            return false;
        }
        
        // 检查任务状态
        if (task.getStatus() != 1) {
            return false;
        }
        
        // 检查课程ID
        if (courseId != null && !task.getCourseId().equals(courseId)) {
            return false;
        }
        
        // 检查时间范围
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(task.getStartTime()) && !now.isAfter(task.getEndTime());
    }
    
    @Override
    public AttendanceTask getTaskDetail(Long taskId) {
        return getTaskById(taskId);
    }
    
    @Override
    public Page<AttendanceTask> getTasksByTeacherId(String teacherId, Pageable pageable) {
        // 简单实现，可以后续优化为分页查询
        List<AttendanceTask> tasks = getTasksByTeacherId(teacherId);
        return Page.empty(pageable);
    }
    
    /**
     * 生成六位随机考勤码
     */
    private String generateTaskCode() {
        Random random = new Random();
        String taskCode;
        int attempts = 0;
        int maxAttempts = 100;
        
        do {
            // 生成6位数字考勤码
            taskCode = String.format("%06d", random.nextInt(1000000));
            attempts++;
            
            if (attempts >= maxAttempts) {
                throw new RuntimeException("无法生成唯一的考勤码，请稍后重试");
            }
        } while (taskRepository.findByTaskCode(taskCode).isPresent());
        
        return taskCode;
    }
}
