package com.example.attendance.entity;

public class AttendanceRecord {
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

    // getter/setter
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
