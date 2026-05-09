package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "course_selection")
public class CourseSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    @Column(name = "student_name", length = 50)
    private String studentName;

    @Column(name = "course_id", nullable = false, length = 20)
    private String courseId;

    @Column(name = "course_name", length = 100)
    private String courseName;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "status", columnDefinition = "tinyint default 1")
    private Integer status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = 1;
        }
    }
}