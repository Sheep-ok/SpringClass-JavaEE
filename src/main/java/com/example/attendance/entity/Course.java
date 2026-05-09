package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false, unique = true, length = 20)
    private String courseId;

    @Column(name = "course_name", nullable = false, length = 100)
    private String courseName;

    @Column(name = "teacher_id", length = 20)
    private String teacherId;

    @Column(name = "teacher_name", length = 50)
    private String teacherName;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "week_day")
    private Integer weekDay;

    @Column(name = "time_slot", length = 20)
    private String timeSlot;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}