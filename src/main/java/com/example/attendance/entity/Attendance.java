package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    @Column(name = "student_name", nullable = false, length = 50)
    private String studentName;

    @Column(name = "course_id", nullable = false, length = 20)
    private String courseId;

    @Column(name = "sign_in_id", nullable = false, length = 20)
    private String signInId;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "student_status", columnDefinition = "tinyint default 1")
    private Integer studentStatus;

    @Column(name = "ip_address", length = 15)
    private String ipAddress;

    @Column(name = "create_time", columnDefinition = "datetime default getdate()")
    private LocalDateTime createTime;
}