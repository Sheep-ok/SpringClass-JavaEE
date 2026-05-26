package com.example.attendance.service;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.ImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelService {

    /**
     * 解析Excel文件，提取考勤数据
     * @param file Excel文件
     * @param courseId 课程ID
     * @return 导入结果报告
     */
    ImportResult parseAttendanceExcel(MultipartFile file, String courseId);

    /**
     * 获取解析后的考勤数据列表
     * @return 考勤数据列表
     */
    List<Attendance> getParsedAttendanceList();

    /**
     * 清空解析缓存
     */
    void clearCache();
}