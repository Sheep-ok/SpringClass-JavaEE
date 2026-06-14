package com.example.attendance.service.impl;

import com.example.attendance.config.FileUploadConfig;
import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.ImportResult;
import com.example.attendance.service.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ExcelServiceImpl implements ExcelService {

    // 支持的日期格式
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };

    // 学生ID正则（假设为学号格式）
    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[0-9]{8,20}$");

    // 状态值允许范围
    private static final int[] VALID_STATUS = {1, 2, 3, 4};

    // 解析结果缓存
    private List<Attendance> parsedAttendanceList = new ArrayList<>();

    @Override
    public ImportResult parseAttendanceExcel(MultipartFile file, String courseId) {
        // 清空缓存
        clearCache();
        
        ImportResult result = new ImportResult();
        List<Attendance> attendanceList = new ArrayList<>();
        List<ImportResult.AttendancePreview> previewData = new ArrayList<>();
        
        try (InputStream is = file.getInputStream()) {
            Workbook workbook = createWorkbook(file, is);
            Sheet sheet = workbook.getSheetAt(0);
            
            int totalRows = sheet.getLastRowNum(); // 不含标题行
            result.setTotalCount(totalRows);
            
            // 从第二行开始读取（第一行为标题）
            for (int rowIndex = 1; rowIndex <= totalRows; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }
                
                try {
                    Attendance attendance = parseRow(row, courseId, result, rowIndex + 1);
                    if (attendance != null) {
                        attendanceList.add(attendance);
                        result.setSuccessCount(result.getSuccessCount() + 1);
                        
                        // 生成预览数据（最多5条）
                        if (previewData.size() < 5) {
                            ImportResult.AttendancePreview preview = new ImportResult.AttendancePreview();
                            preview.setStudentId(attendance.getStudentId());
                            preview.setStudentName(attendance.getStudentName());
                            preview.setCheckInTime(attendance.getCheckInTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            preview.setStatus(attendance.getStudentStatus());
                            preview.setStatusName(getStatusName(attendance.getStudentStatus()));
                            preview.setReason(attendance.getReason());
                            previewData.add(preview);
                        }
                    }
                } catch (Exception e) {
                    result.addFailRecord(rowIndex + 1, "", "", "解析异常: " + e.getMessage());
                }
            }
            
            workbook.close();
            parsedAttendanceList = attendanceList;
            result.setPreviewData(previewData);
            result.calculateResult();
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("文件解析失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 获取状态名称
     */
    private String getStatusName(int status) {
        return switch (status) {
            case 1 -> "签到";
            case 2 -> "迟到";
            case 3 -> "缺勤";
            case 4 -> "请假";
            default -> "未知";
        };
    }

    @Override
    public List<Attendance> getParsedAttendanceList() {
        return new ArrayList<>(parsedAttendanceList);
    }

    @Override
    public void clearCache() {
        parsedAttendanceList.clear();
    }

    /**
     * 根据文件类型创建Workbook
     */
    private Workbook createWorkbook(MultipartFile file, InputStream is) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(is);
        } else if (fileName != null && fileName.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(is);
        } else {
            throw new IllegalArgumentException("不支持的文件格式");
        }
    }

    /**
     * 判断是否为空行
     */
    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 解析单行数据
     * 列定义：A-学号, B-姓名, C-性别, D-班级, E-状态, F-原因
     * 签到时间使用上传文件的当前时间
     */
    private Attendance parseRow(Row row, String courseId, ImportResult result, int rowNum) {
        // 列定义：学号、姓名、性别、班级、状态、原因
        String studentId = getCellValueAsString(row.getCell(0));
        String studentName = getCellValueAsString(row.getCell(1));
        String gender = getCellValueAsString(row.getCell(2));
        String className = getCellValueAsString(row.getCell(3));
        String statusStr = getCellValueAsString(row.getCell(4));
        String reason = getCellValueAsString(row.getCell(5));

        // 验证必填字段
        if (studentId == null || studentId.trim().isEmpty()) {
            result.addFailRecord(rowNum, "", studentName, "学号不能为空");
            return null;
        }
        
        if (studentName == null || studentName.trim().isEmpty()) {
            result.addFailRecord(rowNum, studentId, "", "姓名不能为空");
            return null;
        }

        // 验证学生ID格式
        if (!STUDENT_ID_PATTERN.matcher(studentId.trim()).matches()) {
            result.addFailRecord(rowNum, studentId, studentName, "学号格式不正确，应为8-20位数字");
            return null;
        }

        // 验证状态值（可选，默认为签到）
        Integer status = parseStatus(statusStr);
        if (status == null) {
            result.addFailRecord(rowNum, studentId, studentName, 
                    "状态值不正确，应为1(签到)、2(迟到)、3(早退)、4(请假)");
            return null;
        }

        // 创建考勤记录，签到时间使用当前时间
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId.trim());
        attendance.setStudentName(studentName.trim());
        attendance.setCourseId(courseId);
        attendance.setSignInId(UUID.randomUUID().toString().substring(0, 8));
        attendance.setCheckInTime(LocalDateTime.now()); // 签到时间为上传文件的时间
        attendance.setStudentStatus(status);
        attendance.setReason(reason != null ? reason.trim() : null);
        attendance.setCreateTime(LocalDateTime.now());

        return attendance;
    }

    /**
     * 获取单元格字符串值
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    // 处理数字类型，避免科学计数法
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        yield String.valueOf((long) numValue);
                    } else {
                        yield String.valueOf(numValue);
                    }
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    yield cell.getStringCellValue();
                }
            }
            default -> null;
        };
    }

    /**
     * 解析日期时间，支持多种格式
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return LocalDateTime.now(); // 默认当前时间
        }
        
        String trimmedStr = dateTimeStr.trim();
        
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(trimmedStr, formatter);
            } catch (DateTimeParseException e) {
                // 尝试下一个格式
            }
        }
        
        return null;
    }

    /**
     * 解析状态值
     */
    private Integer parseStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return 1; // 默认签到
        }
        
        try {
            int status = Integer.parseInt(statusStr.trim());
            for (int valid : VALID_STATUS) {
                if (valid == status) {
                    return status;
                }
            }
        } catch (NumberFormatException e) {
            // 尝试中文状态
            String chineseStatus = statusStr.trim();
            return switch (chineseStatus) {
                case "签到", "正常" -> 1;
                case "迟到" -> 2;
                case "早退" -> 3;
                case "请假" -> 4;
                default -> null;
            };
        }
        
        return null;
    }
}