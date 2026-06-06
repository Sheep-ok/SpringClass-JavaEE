package com.example.attendance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

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

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @PostLoad
    @PrePersist
    @PreUpdate
    public void calcAge() {
        if (this.birthDate != null) {
            this.age = Period.between(this.birthDate, LocalDate.now()).getYears();
        }
    }
}