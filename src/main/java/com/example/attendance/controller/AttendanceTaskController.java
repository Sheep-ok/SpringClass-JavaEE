package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.AttendanceTask;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.CourseSelection;
import com.example.attendance.entity.User;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.CourseSelectionRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.AttendanceTaskService;
import com.example.attendance.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/task")
public class AttendanceTaskController {
    
    private static final Logger logger = LoggerFactory.getLogger(AttendanceTaskController.class);
    private final AttendanceTaskService taskService;
    private final AttendanceRepository attendanceRepository;
    private final CourseRepository courseRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    
    public AttendanceTaskController(AttendanceTaskService taskService,
                                   AttendanceRepository attendanceRepository,
                                   CourseRepository courseRepository,
                                   CourseSelectionRepository courseSelectionRepository,
                                   StudentRepository studentRepository,
                                   UserRepository userRepository) {
        this.taskService = taskService;
        this.attendanceRepository = attendanceRepository;
        this.courseRepository = courseRepository;
        this.courseSelectionRepository = courseSelectionRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * 教师发布考勤任务
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/create")
    public Result<AttendanceTask> createTask(
            @RequestParam String courseId,
            @RequestParam String teacherId,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(required = false) String description) {
        
        try {
            // 验证课程是否存在
            Optional<Course> courseOpt = courseRepository.findByCourseId(courseId);
            if (courseOpt.isEmpty()) {
                return Result.error("课程不存在");
            }
            
            Course course = courseOpt.get();
            
            // 解析时间
            LocalDateTime start = LocalDateTime.parse(startTime);
            LocalDateTime end = LocalDateTime.parse(endTime);
            
            // 创建考勤任务
            AttendanceTask task = new AttendanceTask();
            task.setCourseId(courseId);
            task.setCourseName(course.getCourseName());
            task.setTeacherId(teacherId);
            task.setTeacherName(course.getTeacherName());
            task.setStartTime(start);
            task.setEndTime(end);
            task.setDescription(description);
            task.setStatus(1); // 进行中
            
            AttendanceTask savedTask = taskService.createTask(task);
            logger.info("教师 {} 发布考勤任务成功，考勤码: {}", teacherId, savedTask.getTaskCode());
            
            // 为该课程所有选课学生创建初始考勤记录（student_status=3, check_status=3 缺勤）
            createInitialAttendanceRecords(savedTask);
            
            return Result.success(savedTask);
        } catch (Exception e) {
            logger.error("创建考勤任务失败", e);
            return Result.error("创建考勤任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取教师的考勤任务列表
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/teacher/{teacherId}")
    public Result<List<AttendanceTask>> getTeacherTasks(@PathVariable String teacherId) {
        List<AttendanceTask> tasks = taskService.getTasksByTeacherId(teacherId);
        return Result.success(tasks);
    }
    
    /**
     * 获取考勤任务详情
     */
    @GetMapping("/{taskId}")
    public Result<AttendanceTask> getTaskDetail(@PathVariable Long taskId) {
        AttendanceTask task = taskService.getTaskById(taskId);
        if (task == null) {
            return Result.error("考勤任务不存在");
        }
        return Result.success(task);
    }
    
    /**
     * 结束考勤任务
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/end/{taskId}")
    public Result<String> endTask(@PathVariable Long taskId) {
        try {
            taskService.endTask(taskId);
            return Result.success("考勤任务已结束");
        } catch (Exception e) {
            return Result.error("结束考勤任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消考勤任务
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/cancel/{taskId}")
    public Result<String> cancelTask(@PathVariable Long taskId) {
        try {
            taskService.cancelTask(taskId);
            return Result.success("考勤任务已取消");
        } catch (Exception e) {
            return Result.error("取消考勤任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 学生获取可参与的考勤任务
     */
    @GetMapping("/available/{studentId}")
    public Result<List<AttendanceTask>> getAvailableTasks(@PathVariable String studentId) {
        try {
            // 获取学生选课的课程ID列表
            List<CourseSelection> selections = courseSelectionRepository.findByStudentIdAndStatus(studentId, 1);
            if (selections.isEmpty()) {
                return Result.success(List.of());
            }
            
            List<String> courseIds = selections.stream()
                    .map(CourseSelection::getCourseId)
                    .toList();
            
            List<AttendanceTask> tasks = taskService.getAvailableTasksForStudent(courseIds);
            return Result.success(tasks);
        } catch (Exception e) {
            logger.error("获取可参与考勤任务失败", e);
            return Result.error("获取考勤任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 学生签到
     */
    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkIn(
            @RequestParam String studentId,
            @RequestParam String studentName,
            @RequestParam String taskCode,
            HttpServletRequest request) {
        
        try {
            // 验证考勤码
            AttendanceTask task = taskService.getTaskByCode(taskCode);
            if (task == null) {
                return Result.error("考勤码无效");
            }
            
            // 检查任务状态
            if (task.getStatus() != 1) {
                return Result.error("考勤任务已结束或已取消");
            }
            
            LocalDateTime now = LocalDateTime.now();
            
            // 检查是否在时间范围内
            if (now.isBefore(task.getStartTime())) {
                return Result.error("考勤尚未开始");
            }
            
            // 查找已有的考勤记录
            Attendance attendance = attendanceRepository.findByStudentIdAndTaskId(studentId, task.getId());
            if (attendance == null) {
                return Result.error("未找到您的考勤记录，请联系教师");
            }
            
            // 检查是否已经签到过（studentStatus 不是 3 说明已经签到过了）
            if (attendance.getStudentStatus() != null && attendance.getStudentStatus() != 3) {
                return Result.error("您已经签到过了");
            }
            
            // 判断签到状态
            int studentStatus;
            int checkStatus;
            String statusText;
            
            LocalDateTime endTime = task.getEndTime();
            LocalDateTime lateDeadline = endTime.plusMinutes(10); // 结束时间后10分钟
            
            if (now.isAfter(lateDeadline)) {
                // 超过结束时间10分钟，算缺勤
                studentStatus = 3;
                checkStatus = 3;
                statusText = "缺勤";
            } else if (now.isAfter(endTime)) {
                // 在结束时间后10分钟内，算迟到
                studentStatus = 2;
                checkStatus = 2;
                statusText = "迟到";
            } else {
                // 正常签到
                studentStatus = 1;
                checkStatus = 1;
                statusText = "正常签到";
            }
            
            // 更新已有考勤记录
            attendance.setCheckInTime(now);
            attendance.setStudentStatus(studentStatus);
            attendance.setCheckStatus(checkStatus);
            attendance.setIpAddress(getClientIpAddress(request));
            attendance.setReason(null); // 清除初始的"未签到"原因
            
            attendanceRepository.save(attendance);
            
            logger.info("学生 {} 签到成功，考勤码: {}, 状态: {}", studentId, taskCode, statusText);
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", checkStatus);
            result.put("statusText", statusText);
            result.put("taskId", task.getId());
            result.put("courseName", task.getCourseName());
            result.put("checkInTime", now);
            
            return Result.success(result);
        } catch (Exception e) {
            logger.error("签到失败", e);
            return Result.error("签到失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证考勤码
     */
    @GetMapping("/validate")
    public Result<Map<String, Object>> validateTaskCode(
            @RequestParam String taskCode,
            @RequestParam(required = false) String courseId) {
        
        AttendanceTask task = taskService.getTaskByCode(taskCode);
        if (task == null) {
            return Result.error("考勤码无效");
        }
        
        boolean isValid = taskService.validateTaskCode(taskCode, courseId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("valid", isValid);
        result.put("task", task);
        
        if (!isValid) {
            if (task.getStatus() != 1) {
                result.put("message", "考勤任务已结束或已取消");
            } else {
                LocalDateTime now = LocalDateTime.now();
                if (now.isBefore(task.getStartTime())) {
                    result.put("message", "考勤尚未开始");
                } else if (now.isAfter(task.getEndTime())) {
                    result.put("message", "考勤已结束");
                } else {
                    result.put("message", "考勤码无效");
                }
            }
        }
        
        return Result.success(result);
    }
    
    /**
     * 获取考勤任务的签到统计
     */
    @GetMapping("/stats/{taskId}")
    public Result<Map<String, Object>> getTaskStats(@PathVariable Long taskId) {
        try {
            AttendanceTask task = taskService.getTaskById(taskId);
            if (task == null) {
                return Result.error("考勤任务不存在");
            }
            
            // 获取该任务的所有签到记录
            List<Attendance> records = attendanceRepository.findAll();
            List<Attendance> taskRecords = records.stream()
                    .filter(a -> a.getTaskId() != null && a.getTaskId().equals(taskId))
                    .toList();
            
            // 统计
            int totalCount = taskRecords.size();
            int normalCount = (int) taskRecords.stream().filter(a -> a.getCheckStatus() == 1).count();
            int lateCount = (int) taskRecords.stream().filter(a -> a.getCheckStatus() == 2).count();
            int absentCount = (int) taskRecords.stream().filter(a -> a.getCheckStatus() == 3).count();
            
            // 获取该课程应签到人数
            List<CourseSelection> selections = courseSelectionRepository.findByCourseIdAndStatus(task.getCourseId(), 1);
            int expectedCount = selections.size();
            int notCheckedCount = expectedCount - totalCount;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("taskId", taskId);
            stats.put("taskCode", task.getTaskCode());
            stats.put("courseName", task.getCourseName());
            stats.put("expectedCount", expectedCount);
            stats.put("totalCount", totalCount);
            stats.put("normalCount", normalCount);
            stats.put("lateCount", lateCount);
            stats.put("absentCount", absentCount);
            stats.put("notCheckedCount", notCheckedCount);
            
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取考勤统计失败", e);
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取考勤任务的签到记录列表
     */
    @GetMapping("/records/{taskId}")
    public Result<List<Attendance>> getTaskRecords(@PathVariable Long taskId) {
        try {
            List<Attendance> allRecords = attendanceRepository.findAll();
            List<Attendance> taskRecords = allRecords.stream()
                    .filter(a -> a.getTaskId() != null && a.getTaskId().equals(taskId))
                    .toList();
            
            return Result.success(taskRecords);
        } catch (Exception e) {
            logger.error("获取签到记录失败", e);
            return Result.error("获取记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 为考勤任务创建初始考勤记录（所有选课学生，初始状态为缺勤）
     */
    private void createInitialAttendanceRecords(AttendanceTask task) {
        try {
            List<CourseSelection> selections = courseSelectionRepository.findByCourseIdAndStatus(task.getCourseId(), 1);
            
            if (selections.isEmpty()) {
                logger.warn("课程 {} 没有选课学生", task.getCourseId());
                return;
            }
            
            List<Attendance> attendanceList = new ArrayList<>();
            String signInId = UUID.randomUUID().toString().substring(0, 8);
            
            for (CourseSelection selection : selections) {
                Attendance attendance = new Attendance();
                String studentId = selection.getStudentId();
                attendance.setStudentId(studentId);
                
                // 从 User 表获取真实姓名
                String studentName = getStudentRealName(studentId);
                attendance.setStudentName(studentName);
                
                attendance.setCourseId(task.getCourseId());
                attendance.setSignInId(signInId);
                attendance.setCheckInTime(task.getEndTime()); // 使用任务结束时间作为初始时间
                attendance.setStudentStatus(3); // 初始状态：缺勤
                attendance.setTaskId(task.getId());
                attendance.setCheckStatus(3); // 初始签到状态：缺勤
                attendance.setCreateTime(LocalDateTime.now());
                attendance.setReason("未签到");
                attendanceList.add(attendance);
            }
            
            attendanceRepository.saveAll(attendanceList);
            logger.info("为考勤任务 {} 创建了 {} 条初始考勤记录", task.getId(), attendanceList.size());
        } catch (Exception e) {
            logger.error("创建初始考勤记录失败，任务ID: {}", task.getId(), e);
        }
    }
    
    /**
     * 从 User 表获取学生真实姓名
     */
    private String getStudentRealName(String studentId) {
        Optional<User> userOpt = userRepository.findByUsername(studentId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getRealName() != null && !user.getRealName().isEmpty()) {
                return user.getRealName();
            }
        }
        return studentId; // 如果没有找到真实姓名，使用学号
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
