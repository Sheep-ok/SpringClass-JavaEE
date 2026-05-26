package com.example.attendance.controller;

import com.example.attendance.service.StatisticsService;
import com.example.attendance.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @PostMapping
    public Result<Map<String, Object>> getStatistics(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String courseId = params.get("courseId");
        String className = params.get("className");
        String startDate = params.get("startDate");
        String endDate = params.get("endDate");
        
        return statisticsService.getStatistics(username, courseId, className, startDate, endDate);
    }
}
