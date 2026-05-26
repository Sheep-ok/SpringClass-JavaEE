package com.example.attendance.service;

import com.example.attendance.util.Result;

import java.util.Map;

public interface StatisticsService {
    Result<Map<String, Object>> getStatistics(String username, String courseId, String className, String startDate, String endDate);
}
