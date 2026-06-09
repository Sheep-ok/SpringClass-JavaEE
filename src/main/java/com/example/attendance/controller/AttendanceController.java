package com.example.attendance.controller;

import com.example.attendance.config.FileUploadConfig;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.ImportResult;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.service.AttendanceService;
import com.example.attendance.service.ExcelService;
import com.example.attendance.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final ExcelService excelService;
    private final CourseRepository courseRepository;

    public AttendanceController(AttendanceService attendanceService, ExcelService excelService, 
                               CourseRepository courseRepository) {
        this.attendanceService = attendanceService;
        this.excelService = excelService;
        this.courseRepository = courseRepository;
    }

    @GetMapping("/page/student/{studentId}")
    public Result<Page<Attendance>> getAttendancePageByStudentId(
            @PathVariable String studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Attendance> pageResult = attendanceService.getAttendancePageByStudentId(studentId, pageRequest);
        return Result.success(pageResult);
    }

    @GetMapping("/search")
    public Result<Page<Attendance>> searchAttendance(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) Integer studentStatus,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        // 如果传了username（教师），先查出该教师授课的课程ID列表
        List<String> teacherCourseIds = null;
        if (username != null && !username.isBlank()) {
            teacherCourseIds = courseRepository.findByTeacherId(username).stream()
                    .map(Course::getCourseId)
                    .toList();
            // 教师没有课程时直接返回空，避免走到无过滤逻辑
            if (teacherCourseIds.isEmpty()) {
                return Result.success(Page.empty());
            }
        }

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Attendance> result = attendanceService.getAttendancePageByConditions(studentId, courseId, studentStatus, teacherCourseIds, pageRequest);
        return Result.success(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/page")
    public Result<Page<Attendance>> getAttendancePage(Pageable pageable) {
        Page<Attendance> attendancePage = attendanceService.getAttendancePage(pageable);
        return Result.success(attendancePage);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/all")
    public Result<Page<Attendance>> getAllAttendance(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkInTime"));
        Page<Attendance> attendancePage = attendanceService.getAttendancePage(pageRequest);
        return Result.success(attendancePage);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'USER')")
    @GetMapping("/student/{studentId}")
    public Result<Page<Attendance>> getStudentAttendance(
            @PathVariable String studentId, Pageable pageable) {
        Page<Attendance> attendancePage = attendanceService.getAttendancePageByStudentId(studentId, pageable);
        return Result.success(attendancePage);
    }

    @PostMapping("/checkin")
    public Result<String> checkIn(
            @RequestParam String studentId,
            @RequestParam String studentName,
            @RequestParam String courseId,
            HttpServletRequest request) {
        
        String signInId = UUID.randomUUID().toString().substring(0, 8);
        String ipAddress = getClientIpAddress(request);
        
        String result = attendanceService.checkIn(studentId, studentName, courseId, signInId, ipAddress);
        return Result.success(result);
    }

    @PostMapping("/checkin/status")
    public Result<String> checkInWithStatus(
            @RequestParam String studentId,
            @RequestParam String studentName,
            @RequestParam String courseId,
            @RequestParam(defaultValue = "1") Integer studentStatus,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        
        String signInId = UUID.randomUUID().toString().substring(0, 8);
        String ipAddress = getClientIpAddress(request);
        
        String result = attendanceService.checkIn(studentId, studentName, courseId, studentStatus, signInId, ipAddress, reason);
        return Result.success(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/batch")
    public Result<String> batchCheckIn(
            @RequestParam String courseId,
            @RequestParam String studentIds,
            @RequestParam(defaultValue = "1") Integer studentStatus,
            HttpServletRequest request) {
        
        String signInId = UUID.randomUUID().toString().substring(0, 8);
        String ipAddress = getClientIpAddress(request);
        String[] studentIdArray = studentIds.split(",");
        
        String result = attendanceService.batchCheckIn(courseId, studentIdArray, studentStatus, signInId, ipAddress);
        return Result.success(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/{id}")
    public Result<Attendance> getAttendanceById(@PathVariable Long id) {
        Attendance attendance = attendanceService.getAttendanceById(id);
        if (attendance != null) {
            return Result.success(attendance);
        }
        return Result.error("考勤记录不存在");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PutMapping("/{id}")
    public Result<String> updateAttendance(
            @PathVariable Long id,
            @RequestParam Integer studentStatus) {
        String result = attendanceService.updateAttendance(id, studentStatus);
        return Result.success(result);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @DeleteMapping("/{id}")
    public Result<String> deleteAttendance(@PathVariable Long id) {
        String result = attendanceService.deleteAttendance(id);
        return Result.success(result);
    }

    // ==================== 文件上传相关接口 ====================
    
    /**
     * 上传Excel文件并预览数据（不保存）
     * 教师和管理员可使用
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/upload/preview")
    public Result<ImportResult> uploadAndPreview(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") String courseId) {
        
        // 验证课程是否存在
        if (!validateCourse(courseId)) {
            return Result.error("课程ID不存在: " + courseId);
        }
        
        // 文件验证
        String validationError = validateFile(file);
        if (validationError != null) {
            return Result.error(validationError);
        }
        
        try {
            ImportResult result = excelService.parseAttendanceExcel(file, courseId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("文件解析失败: " + e.getMessage());
        }
    }

    /**
     * 上传Excel文件并导入数据
     * 教师和管理员可使用
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @PostMapping("/upload/import")
    public Result<ImportResult> uploadAndImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseId") String courseId) {
        
        // 验证课程是否存在
        Course course = validateCourseWithInfo(courseId);
        if (course == null) {
            return Result.error("课程ID不存在: " + courseId);
        }
        
        // 文件验证
        String validationError = validateFile(file);
        if (validationError != null) {
            return Result.error(validationError);
        }
        
        try {
            // 解析Excel
            ImportResult parseResult = excelService.parseAttendanceExcel(file, courseId);
            
            // 如果有成功解析的数据，保存到数据库
            List<Attendance> attendanceList = excelService.getParsedAttendanceList();
            if (!attendanceList.isEmpty()) {
                ImportResult importResult = attendanceService.batchImportAttendance(attendanceList);
                return Result.success(importResult);
            }
            
            return Result.success(parseResult);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        } finally {
            excelService.clearCache();
        }
    }

    /**
     * 检查文件上传进度（模拟）
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/upload/progress/{uploadId}")
    public Result<Map<String, Object>> getUploadProgress(@PathVariable String uploadId) {
        // 实际项目中可以使用Session或Redis存储上传进度
        Map<String, Object> progress = new HashMap<>();
        progress.put("uploadId", uploadId);
        progress.put("progress", 100); // 模拟完成状态
        progress.put("status", "completed");
        progress.put("message", "上传完成");
        return Result.success(progress);
    }

    /**
     * 获取上传模板信息
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    @GetMapping("/upload/template")
    public Result<Map<String, Object>> getUploadTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put("maxFileSize", FileUploadConfig.MAX_FILE_SIZE / 1024 / 1024 + "MB");
        template.put("allowedExtensions", FileUploadConfig.ALLOWED_EXTENSIONS);
        template.put("columns", new String[]{"学号", "姓名", "签到时间", "状态", "备注"});
        template.put("statusOptions", new Map[]{
            Map.of("value", 1, "label", "签到"),
            Map.of("value", 2, "label", "迟到"),
            Map.of("value", 3, "label", "早退"),
            Map.of("value", 4, "label", "请假")
        });
        template.put("dateFormats", new String[]{
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm",
            "yyyy年MM月dd日 HH:mm:ss",
            "yyyy年MM月dd日 HH:mm"
        });
        return Result.success(template);
    }

    /**
     * 文件验证
     */
    private String validateFile(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            return "请选择要上传的文件";
        }
        
        // 检查文件大小
        if (file.getSize() > FileUploadConfig.MAX_FILE_SIZE) {
            return "文件大小超过限制，最大允许上传 " + (FileUploadConfig.MAX_FILE_SIZE / 1024 / 1024) + "MB";
        }
        
        // 检查文件扩展名
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return "文件名不能为空";
        }
        
        String lowerFileName = fileName.toLowerCase();
        boolean validExtension = false;
        for (String ext : FileUploadConfig.ALLOWED_EXTENSIONS) {
            if (lowerFileName.endsWith(ext)) {
                validExtension = true;
                break;
            }
        }
        
        if (!validExtension) {
            return "不支持的文件格式，仅支持Excel文件(.xls, .xlsx)";
        }
        
        // 检查MIME类型
        String contentType = file.getContentType();
        boolean validMimeType = false;
        if (contentType != null) {
            for (String mime : FileUploadConfig.ALLOWED_MIME_TYPES) {
                if (contentType.equalsIgnoreCase(mime)) {
                    validMimeType = true;
                    break;
                }
            }
        }
        
        // 额外检查文件内容（尝试读取文件头）
        if (!validMimeType) {
            try {
                byte[] header = new byte[8];
                file.getInputStream().read(header);
                String headerHex = bytesToHex(header);
                
                // 检查Excel文件签名
                // xlsx: 50 4B 03 04 (ZIP格式)
                // xls: D0 CF 11 E0 (OLE2格式)
                if (!headerHex.startsWith("504b0304") && !headerHex.startsWith("d0cf11e0")) {
                    return "文件内容不是有效的Excel文件";
                }
            } catch (IOException e) {
                return "无法读取文件内容";
            }
        }
        
        return null; // 验证通过
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 验证课程是否存在
     */
    private boolean validateCourse(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            return false;
        }
        return courseRepository.findByCourseId(courseId.trim()).isPresent();
    }

    /**
     * 验证课程是否存在并返回课程信息
     */
    private Course validateCourseWithInfo(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            return null;
        }
        return courseRepository.findByCourseId(courseId.trim()).orElse(null);
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