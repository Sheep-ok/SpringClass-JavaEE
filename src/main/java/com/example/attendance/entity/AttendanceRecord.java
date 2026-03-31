package com.example.attendance.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AttendanceRecord {
    // getter/setter
    private String studentId; // 学生学号
    private String date;      // 考勤日期
    private String status;    // 考勤状态（如：正常/迟到/缺勤）

    // 无参构造
    public AttendanceRecord() {}

    // 全参构造
    public AttendanceRecord(String studentId, String date, String status) {
        this.studentId = studentId;
        this.date = date;
        this.status = status;
    }

}
