package com.example.attendance.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportResult {

    // 总记录数
    private int totalCount;
    
    // 成功导入数
    private int successCount;
    
    // 失败数
    private int failCount;
    
    // 失败详情列表
    private List<FailRecord> failRecords = new ArrayList<>();
    
    // 导入是否成功
    private boolean success;
    
    // 错误消息
    private String message;
    
    // 预览数据列表（用于前端展示）
    private List<AttendancePreview> previewData;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailRecord {
        // 行号
        private int rowNumber;
        
        // 学生ID
        private String studentId;
        
        // 学生姓名
        private String studentName;
        
        // 失败原因
        private String reason;
    }

    // 考勤预览数据（用于前端展示）
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AttendancePreview {
        // 学号
        private String studentId;
        
        // 姓名
        private String studentName;
        
        // 签到时间
        private String checkInTime;
        
        // 状态（1-签到，2-迟到，3-早退，4-请假）
        private int status;
        
        // 状态名称
        private String statusName;
        
        // 备注
        private String reason;
    }

    public void addFailRecord(int rowNumber, String studentId, String studentName, String reason) {
        if (failRecords == null) {
            failRecords = new ArrayList<>();
        }
        failRecords.add(new FailRecord(rowNumber, studentId, studentName, reason));
        this.failCount++;
    }

    public void calculateResult() {
        this.success = failCount == 0;
        if (success) {
            this.message = String.format("导入成功，共导入 %d 条记录", successCount);
        } else {
            this.message = String.format("导入完成，成功 %d 条，失败 %d 条", successCount, failCount);
        }
    }
}