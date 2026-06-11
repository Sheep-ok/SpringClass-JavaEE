package com.example.attendance.service.impl;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.ImportResult;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.service.AttendanceService;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);

    private final AttendanceRepository attendanceRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    @Override
    public Page<Attendance> getAttendancePage(Pageable pageable) {
        logger.debug("分页查询考勤记录，页码: {}, 每页数量: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Attendance> page = attendanceRepository.findAll(pageable);
        logger.info("考勤记录分页查询完成，总数: {}", page.getTotalElements());
        return page;
    }

    @Override
    public Page<Attendance> getAttendancePageByStudentId(String studentId, Pageable pageable) {
        logger.debug("按学生ID查询考勤记录，学生ID: {}", studentId);
        Page<Attendance> page = attendanceRepository.findByStudentId(studentId, pageable);
        logger.info("学生 {} 考勤记录查询完成，总数: {}", studentId, page.getTotalElements());
        return page;
    }

    @Override
    public Page<Attendance> getAttendancePageByConditions(String studentId, String courseId, Integer studentStatus, List<String> courseIds, Pageable pageable) {
        return getAttendancePageByConditions(studentId, null, courseId, null, studentStatus, courseIds, pageable);
    }

    @Override
    public Page<Attendance> getAttendancePageByConditions(String studentId, String studentName, String courseId, Integer checkStatus, Integer studentStatus, List<String> courseIds, Pageable pageable) {
        logger.debug("按条件查询考勤记录，学生ID: {}, 姓名: {}, 课程ID: {}, 考勤状态: {}, 旧状态: {}, courseIds: {}", studentId, studentName, courseId, checkStatus, studentStatus, courseIds);
        // 防御：courseIds 非 null 但为空，说明教师无课程，直接返回空页
        if (courseIds != null && courseIds.isEmpty()) {
            return Page.empty();
        }
        Page<Attendance> page = attendanceRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (studentId != null && !studentId.isBlank()) {
                predicates.add(cb.equal(root.get("studentId"), studentId));
            }

            if (studentName != null && !studentName.isBlank()) {
                predicates.add(cb.like(root.get("studentName"), "%" + studentName + "%"));
            }

            if (courseId != null && !courseId.isBlank()) {
                predicates.add(cb.equal(root.get("courseId"), courseId));
            }

            if (checkStatus != null) {
                predicates.add(cb.equal(root.get("checkStatus"), checkStatus));
            }

            if (studentStatus != null) {
                predicates.add(cb.equal(root.get("studentStatus"), studentStatus));
            }

            if (courseIds != null && !courseIds.isEmpty()) {
                predicates.add(root.get("courseId").in(courseIds));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        }, pageable);
        logger.info("条件查询考勤记录完成，总数: {}", page.getTotalElements());
        return page;
    }

    @Override
    public String checkIn(String studentId, String studentName, String courseId, String signInId, String ipAddress) {
        logger.info("学生签到，学生ID: {}, 学生姓名: {}, 课程ID: {}", studentId, studentName, courseId);
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setStudentName(studentName);
        attendance.setCourseId(courseId);
        attendance.setSignInId(signInId);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStudentStatus(1);
        attendance.setIpAddress(ipAddress);
        attendance.setCreateTime(LocalDateTime.now());

        attendanceRepository.save(attendance);
        logger.info("学生签到成功，学生ID: {}", studentId);
        return "签到成功";
    }

    @Override
    public String checkIn(String studentId, String studentName, String courseId, Integer studentStatus, String signInId, String ipAddress) {
        return checkIn(studentId, studentName, courseId, studentStatus, signInId, ipAddress, null);
    }

    @Override
    public String checkIn(String studentId, String studentName, String courseId, Integer studentStatus, String signInId, String ipAddress, String reason) {
        logger.info("考勤登记，学生ID: {}, 学生姓名: {}, 课程ID: {}, 状态: {}", studentId, studentName, courseId, studentStatus);
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setStudentName(studentName);
        attendance.setCourseId(courseId);
        attendance.setSignInId(signInId);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStudentStatus(studentStatus != null ? studentStatus : 1);
        attendance.setIpAddress(ipAddress);
        attendance.setCreateTime(LocalDateTime.now());
        attendance.setReason(reason);

        attendanceRepository.save(attendance);
        
        String statusMsg = switch (studentStatus != null ? studentStatus : 1) {
            case 1 -> "签到成功";
            case 2 -> "迟到登记成功";
            case 3 -> "早退登记成功";
            case 4 -> "请假登记成功";
            default -> "考勤登记成功";
        };
        logger.info("考勤登记成功，学生ID: {}, 状态: {}", studentId, statusMsg);
        return statusMsg;
    }

    @Override
    public String batchCheckIn(String courseId, String[] studentIds, Integer studentStatus, String signInId, String ipAddress) {
        logger.info("批量签到，课程ID: {}, 学生数量: {}, 状态: {}", courseId, studentIds.length, studentStatus);
        String batchSignInId = UUID.randomUUID().toString().substring(0, 8);
        LocalDateTime now = LocalDateTime.now();
        
        List<Attendance> attendanceList = new ArrayList<>();
        for (String studentId : studentIds) {
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setStudentName("");
            attendance.setCourseId(courseId);
            attendance.setSignInId(batchSignInId);
            attendance.setCheckInTime(now);
            attendance.setStudentStatus(studentStatus != null ? studentStatus : 1);
            attendance.setIpAddress(ipAddress);
            attendance.setCreateTime(now);
            attendanceList.add(attendance);
        }
        
        attendanceRepository.saveAll(attendanceList);
        
        String statusMsg = switch (studentStatus != null ? studentStatus : 1) {
            case 1 -> "批量签到成功";
            case 2 -> "批量缺勤登记成功";
            case 3 -> "批量早退登记成功";
            case 4 -> "批量请假登记成功";
            default -> "批量考勤登记成功";
        };
        logger.info("批量签到完成，课程ID: {}, 成功数量: {}", courseId, studentIds.length);
        return statusMsg;
    }

    @Override
    public String updateAttendance(Long id, Integer studentStatus) {
        logger.info("更新考勤状态，记录ID: {}, 状态: {}", id, studentStatus);
        return attendanceRepository.findById(id)
            .map(attendance -> {
                attendance.setStudentStatus(studentStatus);
                attendanceRepository.save(attendance);
                
                String statusMsg = switch (studentStatus) {
                    case 1 -> "已修改为签到";
                    case 2 -> "已修改为迟到";
                    case 3 -> "已修改为早退";
                    case 4 -> "已修改为请假";
                    default -> "考勤状态已更新";
                };
                logger.info("考勤状态更新成功，记录ID: {}, 状态: {}", id, statusMsg);
                return statusMsg;
            })
            .orElseGet(() -> {
                logger.warn("更新考勤失败，记录不存在，记录ID: {}", id);
                return "考勤记录不存在";
            });
    }

    @Override
    @Transactional
    public String updateAttendanceStatus(Long id, Integer checkStatus, String reason) {
        logger.info("更新考勤记录，记录ID: {}, 考勤状态: {}, 原因: {}", id, checkStatus, reason);
        return attendanceRepository.findById(id)
            .map(attendance -> {
                attendance.setCheckStatus(checkStatus);
                if (reason != null && !reason.isBlank()) {
                    attendance.setReason(reason);
                }
                attendanceRepository.save(attendance);
                
                String statusMsg = switch (checkStatus) {
                    case 1 -> "已修改为正常签到";
                    case 2 -> "已修改为迟到";
                    case 3 -> "已修改为缺勤";
                    case 4 -> "已修改为请假";
                    default -> "考勤状态已更新";
                };
                logger.info("考勤记录更新成功，记录ID: {}, 状态: {}", id, statusMsg);
                return "更新成功";
            })
            .orElseGet(() -> {
                logger.warn("更新考勤失败，记录不存在，记录ID: {}", id);
                return "考勤记录不存在";
            });
    }

    @Override
    public String deleteAttendance(Long id) {
        logger.info("删除考勤记录，记录ID: {}", id);
        if (attendanceRepository.existsById(id)) {
            attendanceRepository.deleteById(id);
            logger.info("考勤记录删除成功，记录ID: {}", id);
            return "删除成功";
        }
        logger.warn("删除考勤失败，记录不存在，记录ID: {}", id);
        return "考勤记录不存在";
    }

    @Override
    public Attendance getAttendanceById(Long id) {
        logger.debug("查询考勤记录，记录ID: {}", id);
        Attendance attendance = attendanceRepository.findById(id).orElse(null);
        if (attendance != null) {
            logger.debug("查询到考勤记录，记录ID: {}, 学生ID: {}", id, attendance.getStudentId());
        } else {
            logger.debug("未找到考勤记录，记录ID: {}", id);
        }
        return attendance;
    }

    @Override
    @Transactional
    public ImportResult batchImportAttendance(List<Attendance> attendanceList) {
        logger.info("开始批量导入考勤记录，总数: {}", attendanceList.size());
        ImportResult result = new ImportResult();
        result.setTotalCount(attendanceList.size());
        
        List<Attendance> validAttendances = new ArrayList<>();
        
        for (int i = 0; i < attendanceList.size(); i++) {
            Attendance attendance = attendanceList.get(i);
            int rowNum = i + 2;
            
            if (attendance.getStudentId() == null || attendance.getStudentId().trim().isEmpty()) {
                result.addFailRecord(rowNum, "", attendance.getStudentName(), "学生ID不能为空");
                logger.debug("导入失败，第{}行，学生ID为空", rowNum);
                continue;
            }
            
            if (attendance.getStudentName() == null || attendance.getStudentName().trim().isEmpty()) {
                result.addFailRecord(rowNum, attendance.getStudentId(), "", "学生姓名不能为空");
                logger.debug("导入失败，第{}行，学生姓名为空", rowNum);
                continue;
            }
            
            if (attendance.getCourseId() == null || attendance.getCourseId().trim().isEmpty()) {
                result.addFailRecord(rowNum, attendance.getStudentId(), attendance.getStudentName(), "课程ID不能为空");
                logger.debug("导入失败，第{}行，课程ID为空", rowNum);
                continue;
            }
            
            validAttendances.add(attendance);
            result.setSuccessCount(result.getSuccessCount() + 1);
        }
        
        if (!validAttendances.isEmpty()) {
            attendanceRepository.saveAll(validAttendances);
            logger.info("批量导入完成，成功: {}, 失败: {}", result.getSuccessCount(), result.getFailCount());
        }
        
        result.calculateResult();
        return result;
    }
}