package com.example.attendance.config;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.AttendanceTask;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.AttendanceTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：自动处理考勤任务状态和考勤记录同步
 */
@Component
public class AttendanceTaskScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceTaskScheduler.class);
    private final AttendanceTaskRepository taskRepository;
    private final AttendanceRepository attendanceRepository;

    public AttendanceTaskScheduler(AttendanceTaskRepository taskRepository,
                                  AttendanceRepository attendanceRepository) {
        this.taskRepository = taskRepository;
        this.attendanceRepository = attendanceRepository;
    }

    /**
     * 每分钟执行一次：自动结束过期的考勤任务
     * 在宽限结束时间（worse_time）过后才将状态设置为已结束
     */
    @Scheduled(cron = "0 * * * * ?")
    public void autoEndExpiredTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<AttendanceTask> expiredTasks = taskRepository.findExpiredTasks(now);

        for (AttendanceTask task : expiredTasks) {
            task.setStatus(2); // 设置为已结束
            taskRepository.save(task);
            logger.info("自动结束考勤任务，任务ID: {}, 考勤码: {}", task.getId(), task.getTaskCode());
        }
    }

    /**
     * 每5分钟执行一次：同步考勤记录的 check_status 与 student_status
     * 在宽限结束时间（worse_time）过后，将 check_status 更新为跟 student_status 一样
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void syncCheckStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 查找宽限结束时间已过的考勤任务
        List<AttendanceTask> allTasks = taskRepository.findAll();
        List<AttendanceTask> tasksToProcess = allTasks.stream()
                .filter(task -> task.getWorseTime() != null && task.getWorseTime().isBefore(now))
                .toList();

        for (AttendanceTask task : tasksToProcess) {
            syncTaskAttendanceStatus(task);
        }
    }

    /**
     * 同步单个考勤任务的考勤状态
     * 将 check_status 更新为跟 student_status 一样
     */
    private void syncTaskAttendanceStatus(AttendanceTask task) {
        List<Attendance> attendances = attendanceRepository.findByTaskId(task.getId());

        if (attendances.isEmpty()) {
            return;
        }

        int updatedCount = 0;
        for (Attendance attendance : attendances) {
            Integer studentStatus = attendance.getStudentStatus();
            Integer checkStatus = attendance.getCheckStatus();

            // 如果 check_status 与 student_status 不一致，则同步
            if (studentStatus != null && !studentStatus.equals(checkStatus)) {
                attendance.setCheckStatus(studentStatus);
                attendanceRepository.save(attendance);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            logger.info("同步考勤任务 {} 的考勤状态，更新了 {} 条记录", task.getId(), updatedCount);
        }
    }
}
