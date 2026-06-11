package com.example.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableScheduling
@RestController
public class AttendanceSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AttendanceSystemApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello() {
        return "欢迎来到班级考勤管理系统！";
    }

    // 新增的/about接口
    @GetMapping("/about")
    public String about() {
        String name = "杨茜";
        String major = "人工智能";
        return String.format("姓名：%s，专业：%s", name, major);
    }
}