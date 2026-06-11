package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 考勤任务实体类
 * 教师发布的考勤任务，包含考勤码、开始时间、结束时间等信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attendance_task")
public class AttendanceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_code", nullable = false, length = 6, unique = true)
    private String taskCode; // 六位考勤码

    @Column(name = "course_id", nullable = false, length = 20)
    private String courseId; // 课程ID

    @Column(name = "course_name", length = 100)
    private String courseName; // 课程名称

    @Column(name = "teacher_id", nullable = false, length = 20)
    private String teacherId; // 教师ID

    @Column(name = "teacher_name", length = 50)
    private String teacherName; // 教师姓名

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // 签到开始时间

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; // 签到结束时间

    @Column(name = "worse_time")
    private LocalDateTime worseTime; // 宽限结束时间（end_time + 10分钟）

    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status; // 任务状态：1-进行中，2-已结束，3-已取消

    @Column(name = "create_time")
    private LocalDateTime createTime; // 创建时间

    @Column(name = "description", length = 500)
    private String description; // 任务描述

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = 1; // 默认状态为进行中
        }
        // 自动计算宽限结束时间：end_time + 10分钟
        if (this.endTime != null && this.worseTime == null) {
            this.worseTime = this.endTime.plusMinutes(10);
        }
    }
}
