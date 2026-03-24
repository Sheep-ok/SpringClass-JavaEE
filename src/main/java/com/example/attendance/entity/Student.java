package com.example.attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    private String studentId; // 学号
    private String name;      // 姓名
    private String className; // 班级
    private Integer age;      // 年龄

}