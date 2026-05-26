package com.example.attendance.service.impl;

import com.example.attendance.entity.User;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.CourseSelectionRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.StatisticsService;
import com.example.attendance.util.Result;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public StatisticsServiceImpl(AttendanceRepository attendanceRepository,
                                StudentRepository studentRepository,
                                CourseSelectionRepository courseSelectionRepository,
                                UserRepository userRepository,
                                CourseRepository courseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.courseSelectionRepository = courseSelectionRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public Result<Map<String, Object>> getStatistics(String username, String courseId, String className, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        // 获取用户角色（所有用户都有明确角色）
        String role = null;
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            role = userOpt.get().getUserrole();
            System.out.println("找到用户: " + username + ", 角色: '" + role + "'");
            
            // 如果角色为空，返回空数据
            if (role == null || role.trim().isEmpty()) {
                System.out.println("用户角色为空，返回空数据");
                result.put("totalStudents", 0);
                result.put("totalRecords", 0);
                result.put("attendanceRate", 0);
                result.put("lateCount", 0);
                result.put("statusData", Map.of("present", 0L, "late", 0L, "earlyLeave", 0L, "absent", 0L));
                result.put("dailyData", Map.of("dates", List.of(), "present", List.of(), "late", List.of(), "earlyLeave", List.of(), "absent", List.of()));
                result.put("classData", Map.of("classNames", List.of(), "rates", List.of()));
                result.put("courseData", Map.of("courseNames", List.of(), "counts", List.of()));
                return Result.success(result);
            }
        } else {
            System.out.println("未找到用户: " + username);
            // 用户不存在时返回空数据
            result.put("totalStudents", 0);
            result.put("totalRecords", 0);
            result.put("attendanceRate", 0);
            result.put("lateCount", 0);
            result.put("statusData", Map.of("present", 0L, "late", 0L, "earlyLeave", 0L, "absent", 0L));
            result.put("dailyData", Map.of("dates", List.of(), "present", List.of(), "late", List.of(), "earlyLeave", List.of(), "absent", List.of()));
            result.put("classData", Map.of("classNames", List.of(), "rates", List.of()));
            result.put("courseData", Map.of("courseNames", List.of(), "counts", List.of()));
            return Result.success(result);
        }

        // 处理日期参数
        LocalDateTime start = parseDateTime(startDate, true);
        LocalDateTime end = parseDateTime(endDate, false);

        // 处理空字符串参数
        if (courseId != null && courseId.isEmpty()) {
            courseId = null;
        }
        if (className != null && className.isEmpty()) {
            className = null;
        }

        // 如果是教师，只统计其任教的课程
        List<String> teacherCourseIds = new ArrayList<>();
        if ("TEACHER".equals(role.toUpperCase())) {
            teacherCourseIds = courseRepository.findByTeacherId(username)
                    .stream()
                    .map(c -> c.getCourseId())
                    .toList();
            
            System.out.println("教师 " + username + " 的课程列表: " + teacherCourseIds);
            
            // 如果教师没有课程，返回空数据
            if (teacherCourseIds.isEmpty()) {
                System.out.println("教师 " + username + " 没有任何课程，返回空数据");
                result.put("totalStudents", 0);
                result.put("totalRecords", 0);
                result.put("attendanceRate", 0);
                result.put("lateCount", 0);
                result.put("statusData", Map.of("present", 0L, "late", 0L, "earlyLeave", 0L, "absent", 0L));
                result.put("dailyData", Map.of("dates", List.of(), "present", List.of(), "late", List.of(), "earlyLeave", List.of(), "absent", List.of()));
                result.put("classData", Map.of("classNames", List.of(), "rates", List.of()));
                result.put("courseData", Map.of("courseNames", List.of(), "counts", List.of()));
                return Result.success(result);
            }
            
            // 如果指定了课程，检查该课程是否属于该教师
            if (courseId != null) {
                if (!teacherCourseIds.contains(courseId)) {
                    System.out.println("教师 " + username + " 无权查看课程 " + courseId + " 的统计数据");
                    return Result.error("无权查看该课程的统计数据");
                }
            }
        }

        // 确定实际用于统计的课程ID列表
        List<String> targetCourseIds = new ArrayList<>();
        if ("TEACHER".equals(role.toUpperCase())) {
            // 如果指定了课程，只统计该课程的数据；否则统计教师所有课程的数据
            if (courseId != null) {
                targetCourseIds = List.of(courseId);
            } else {
                targetCourseIds = teacherCourseIds;
            }
        }

        // 获取学生总数（教师只统计其课程的学生，管理员统计所有学生）
        long totalStudents;
        if ("TEACHER".equals(role.toUpperCase())) {
            System.out.println("进入教师分支，targetCourseIds: " + targetCourseIds + ", className: " + className);
            if (targetCourseIds.isEmpty()) {
                totalStudents = 0;
            } else if (className != null && !className.isEmpty()) {
                // 教师选择了班级：统计该教师课程中属于该班级的学生
                totalStudents = courseSelectionRepository.countDistinctStudentIdByCourseIdsAndClassName(targetCourseIds, className);
                System.out.println("教师 " + username + " 的课程中属于班级 " + className + " 的学生数: " + totalStudents);
            } else {
                // 教师没选择班级：统计该教师所有课程的学生
                totalStudents = courseSelectionRepository.countDistinctStudentIdByCourseIdInAndStatus(targetCourseIds);
                System.out.println("教师 " + username + " 的课程共有 " + totalStudents + " 名有效选课学生");
            }
        } else if ("ADMIN".equals(role.toUpperCase())) {
            System.out.println("进入管理员分支，courseId: " + courseId + ", className: " + className);
            // 根据课程和班级进行组合筛选
            if (courseId != null && !courseId.isEmpty() && className != null && !className.isEmpty()) {
                // 同时选择了课程和班级：统计选了该课程且属于该班级的学生
                totalStudents = studentRepository.countByCourseIdAndClassName(courseId, className);
                System.out.println("按课程和班级联合过滤后学生总数: " + totalStudents);
            } else if (courseId != null && !courseId.isEmpty()) {
                // 只选择了课程：统计选了该课程的学生
                totalStudents = studentRepository.countByCourseId(courseId);
                System.out.println("按课程过滤后学生总数: " + totalStudents);
            } else if (className != null && !className.isEmpty()) {
                // 只选择了班级：统计该班级的学生
                totalStudents = studentRepository.countByClassName(className);
                System.out.println("按班级过滤后学生总数: " + totalStudents);
            } else {
                // 都没选择：统计所有学生
                totalStudents = studentRepository.count();
                System.out.println("管理员学生总数查询结果: " + totalStudents);
            }
            System.out.println("管理员查看学生总数: " + totalStudents);
        } else {
            // 角色为空且没有课程，返回0
            totalStudents = 0;
            System.out.println("用户角色未知(" + role + ")且没有课程，学生总数为0");
        }
        result.put("totalStudents", totalStudents);

        // 获取考勤记录总数
        long totalRecords;
        if ("TEACHER".equals(role.toUpperCase())) {
            totalRecords = attendanceRepository.countByTimeRangeForTeacherCourses(start, end, targetCourseIds, className);
        } else if ("ADMIN".equals(role.toUpperCase())) {
            totalRecords = attendanceRepository.countByTimeRange(start, end, courseId, className);
        } else {
            // 学生角色返回0
            totalRecords = 0;
        }
        result.put("totalRecords", totalRecords);

        // 获取各状态数量
        Map<String, Long> statusData;
        if ("TEACHER".equals(role.toUpperCase())) {
            statusData = getStatusDataByCourseIds(start, end, targetCourseIds, className);
        } else if ("ADMIN".equals(role.toUpperCase())) {
            statusData = getStatusData(start, end, courseId, className);
        } else {
            // 学生角色返回空数据
            statusData = Map.of("present", 0L, "late", 0L, "earlyLeave", 0L, "absent", 0L);
        }
        result.put("statusData", statusData);

        // 计算出勤率
        double attendanceRate = 0;
        if (totalStudents > 0 && totalRecords > 0) {
            attendanceRate = Math.round((statusData.getOrDefault("present", 0L) * 100.0) / totalRecords * 100) / 100.0;
        }
        result.put("attendanceRate", attendanceRate);
        result.put("lateCount", statusData.getOrDefault("late", 0L));

        // 获取每日数据
        Map<String, Object> dailyData;
        if ("TEACHER".equals(role.toUpperCase())) {
            dailyData = getDailyDataByCourseIds(start, end, targetCourseIds, className);
        } else if ("ADMIN".equals(role.toUpperCase())) {
            dailyData = getDailyData(start, end, courseId, className);
        } else {
            // 学生角色返回空数据
            dailyData = Map.of("dates", List.of(), "present", List.of(), "late", List.of(), "earlyLeave", List.of(), "absent", List.of());
        }
        result.put("dailyData", dailyData);

        // 获取班级数据
        Map<String, Object> classData;
        if ("TEACHER".equals(role.toUpperCase())) {
            classData = getClassDataByCourseIds(start, end, targetCourseIds);
        } else if ("ADMIN".equals(role.toUpperCase())) {
            classData = getClassData(start, end, courseId);
        } else {
            // 学生角色返回空数据
            classData = Map.of("classNames", List.of(), "rates", List.of());
        }
        result.put("classData", classData);

        // 获取课程数据
        Map<String, Object> courseData;
        if ("TEACHER".equals(role.toUpperCase())) {
            courseData = getCourseDataByCourseIds(start, end, className, targetCourseIds);
        } else if ("ADMIN".equals(role.toUpperCase())) {
            courseData = getCourseData(start, end, className);
        } else {
            // 学生角色返回空数据
            courseData = Map.of("courseNames", List.of(), "counts", List.of());
        }
        result.put("courseData", courseData);

        return Result.success(result);
    }

    private Map<String, Long> getStatusData(LocalDateTime start, LocalDateTime end, String courseId, String className) {
        Map<String, Long> statusData = new HashMap<>();
        
        // 签到（状态1）
        long present = attendanceRepository.countByStatusAndTimeRange(1, start, end, courseId, className);
        statusData.put("present", present);
        
        // 迟到（状态2）
        long late = attendanceRepository.countByStatusAndTimeRange(2, start, end, courseId, className);
        statusData.put("late", late);
        
        // 早退（状态3）
        long earlyLeave = attendanceRepository.countByStatusAndTimeRange(3, start, end, courseId, className);
        statusData.put("earlyLeave", earlyLeave);
        
        // 请假（状态4）
        long absent = attendanceRepository.countByStatusAndTimeRange(4, start, end, courseId, className);
        statusData.put("absent", absent);
        
        return statusData;
    }

    private Map<String, Object> getDailyData(LocalDateTime start, LocalDateTime end, String courseId, String className) {
        Map<String, Object> dailyData = new HashMap<>();
        
        List<String> dates = new ArrayList<>();
        List<Long> presentList = new ArrayList<>();
        List<Long> lateList = new ArrayList<>();
        List<Long> earlyLeaveList = new ArrayList<>();
        List<Long> absentList = new ArrayList<>();
        
        // 获取最近7天的数据
        LocalDate currentDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        
        while (!currentDate.isAfter(endDate)) {
            dates.add(formatDate(currentDate));
            
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);
            
            presentList.add(attendanceRepository.countByStatusAndTimeRange(1, dayStart, dayEnd, courseId, className));
            lateList.add(attendanceRepository.countByStatusAndTimeRange(2, dayStart, dayEnd, courseId, className));
            earlyLeaveList.add(attendanceRepository.countByStatusAndTimeRange(3, dayStart, dayEnd, courseId, className));
            absentList.add(attendanceRepository.countByStatusAndTimeRange(4, dayStart, dayEnd, courseId, className));
            
            currentDate = currentDate.plusDays(1);
        }
        
        dailyData.put("dates", dates);
        dailyData.put("present", presentList);
        dailyData.put("late", lateList);
        dailyData.put("earlyLeave", earlyLeaveList);
        dailyData.put("absent", absentList);
        
        return dailyData;
    }

    private Map<String, Object> getClassData(LocalDateTime start, LocalDateTime end, String courseId) {
        Map<String, Object> classData = new HashMap<>();
        
        List<String> classNames = studentRepository.findAllClassNames();
        List<Double> rates = new ArrayList<>();
        
        for (String className : classNames) {
            long total = attendanceRepository.countByTimeRange(start, end, courseId, className);
            long present = attendanceRepository.countByStatusAndTimeRange(1, start, end, courseId, className);
            
            double rate = total > 0 ? Math.round((present * 100.0 / total) * 100) / 100.0 : 0;
            rates.add(rate);
        }
        
        classData.put("classNames", classNames);
        classData.put("rates", rates);
        
        return classData;
    }

    private Map<String, Object> getCourseData(LocalDateTime start, LocalDateTime end, String className) {
        Map<String, Object> courseData = new HashMap<>();
        
        // 获取有考勤记录的课程
        List<Object[]> courseStats = attendanceRepository.getCourseStats(start, end, className);
        
        List<String> courseNames = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        for (Object[] stat : courseStats) {
            String courseId = (String) stat[0];
            String courseName = (String) stat[1];
            Long count = (Long) stat[2];
            
            courseNames.add(courseName != null ? courseName : courseId);
            counts.add(count);
        }
        
        courseData.put("courseNames", courseNames);
        courseData.put("counts", counts);
        
        return courseData;
    }

    private LocalDateTime parseDateTime(String dateStr, boolean isStart) {
        if (dateStr == null || dateStr.isEmpty()) {
            if (isStart) {
                return LocalDateTime.now().minusDays(7);
            } else {
                return LocalDateTime.now();
            }
        }
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return isStart ? date.atStartOfDay() : date.atTime(23, 59, 59);
        } catch (Exception e) {
            return isStart ? LocalDateTime.now().minusDays(7) : LocalDateTime.now();
        }
    }

    private String formatDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        return date.format(formatter);
    }

    // 教师专用方法：根据课程ID列表获取状态数据（只统计选了该教师课程的学生）
    private Map<String, Long> getStatusDataByCourseIds(LocalDateTime start, LocalDateTime end, List<String> courseIds, String className) {
        Map<String, Long> statusData = new HashMap<>();
        
        long present = attendanceRepository.countByStatusForTeacherCourses(1, start, end, courseIds, className);
        long late = attendanceRepository.countByStatusForTeacherCourses(2, start, end, courseIds, className);
        long earlyLeave = attendanceRepository.countByStatusForTeacherCourses(3, start, end, courseIds, className);
        long absent = attendanceRepository.countByStatusForTeacherCourses(4, start, end, courseIds, className);
        
        statusData.put("present", present);
        statusData.put("late", late);
        statusData.put("earlyLeave", earlyLeave);
        statusData.put("absent", absent);
        
        return statusData;
    }

    // 教师专用方法：根据课程ID列表获取每日数据（只统计选了该教师课程的学生）
    private Map<String, Object> getDailyDataByCourseIds(LocalDateTime start, LocalDateTime end, List<String> courseIds, String className) {
        Map<String, Object> dailyData = new HashMap<>();
        
        List<String> dates = new ArrayList<>();
        List<Long> presentList = new ArrayList<>();
        List<Long> lateList = new ArrayList<>();
        List<Long> earlyLeaveList = new ArrayList<>();
        List<Long> absentList = new ArrayList<>();
        
        LocalDate currentDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        
        while (!currentDate.isAfter(endDate)) {
            dates.add(formatDate(currentDate));
            
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);
            
            long dayPresent = attendanceRepository.countByStatusForTeacherCourses(1, dayStart, dayEnd, courseIds, className);
            long dayLate = attendanceRepository.countByStatusForTeacherCourses(2, dayStart, dayEnd, courseIds, className);
            long dayEarlyLeave = attendanceRepository.countByStatusForTeacherCourses(3, dayStart, dayEnd, courseIds, className);
            long dayAbsent = attendanceRepository.countByStatusForTeacherCourses(4, dayStart, dayEnd, courseIds, className);
            
            presentList.add(dayPresent);
            lateList.add(dayLate);
            earlyLeaveList.add(dayEarlyLeave);
            absentList.add(dayAbsent);
            
            currentDate = currentDate.plusDays(1);
        }
        
        dailyData.put("dates", dates);
        dailyData.put("present", presentList);
        dailyData.put("late", lateList);
        dailyData.put("earlyLeave", earlyLeaveList);
        dailyData.put("absent", absentList);
        
        return dailyData;
    }

    // 教师专用方法：根据课程ID列表获取班级数据（只统计选了该教师课程的学生）
    private Map<String, Object> getClassDataByCourseIds(LocalDateTime start, LocalDateTime end, List<String> courseIds) {
        Map<String, Object> classData = new HashMap<>();
        
        // 只获取教师课程涉及的班级
        List<String> classNames = courseSelectionRepository.findDistinctClassNamesByCourseIds(courseIds);
        List<Double> rates = new ArrayList<>();
        
        for (String className : classNames) {
            long total = attendanceRepository.countByTimeRangeForTeacherCourses(start, end, courseIds, className);
            long present = attendanceRepository.countByStatusForTeacherCourses(1, start, end, courseIds, className);
            
            double rate = total > 0 ? Math.round((present * 100.0 / total) * 100) / 100.0 : 0;
            rates.add(rate);
        }
        
        classData.put("classNames", classNames);
        classData.put("rates", rates);
        
        return classData;
    }

    // 教师专用方法：根据课程ID列表获取课程数据（只统计选了该教师课程的学生）
    private Map<String, Object> getCourseDataByCourseIds(LocalDateTime start, LocalDateTime end, String className, List<String> courseIds) {
        Map<String, Object> courseData = new HashMap<>();
        
        List<String> courseNames = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        for (String courseId : courseIds) {
            long count = attendanceRepository.countByTimeRangeForTeacherCourses(start, end, List.of(courseId), className);
            
            // 获取课程名称
            String courseName = courseRepository.findByCourseId(courseId)
                    .map(c -> c.getCourseName())
                    .orElse(courseId);
            
            courseNames.add(courseName);
            counts.add(count);
        }
        
        courseData.put("courseNames", courseNames);
        courseData.put("counts", counts);
        
        return courseData;
    }
}
