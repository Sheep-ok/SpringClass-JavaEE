package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "student")
public class Student {
    @Id
    @Column(name = "student_id", nullable = false, unique = true, length = 20)
    private String studentId;

    @Column(name = "student_name", length = 50)
    private String name;
    @Column(name = "class_name", length = 50)
    private String className;
    private Integer age;
    @Column(name = "gender", length = 10)
    private String gender;
}