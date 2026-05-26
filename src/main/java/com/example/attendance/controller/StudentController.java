package com.example.attendance.controller;

import com.example.attendance.entity.AttendanceRecord;
import com.example.attendance.entity.Student;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.CourseSelectionRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.StudentService;
import com.example.attendance.util.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final CourseSelectionRepository courseSelectionRepository;

    // 构造注入
    public StudentController(StudentService studentService, StudentRepository studentRepository,
                            CourseRepository courseRepository, CourseSelectionRepository courseSelectionRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.courseSelectionRepository = courseSelectionRepository;
    }

    @GetMapping("/info")
    public String getStudentInfo() {
        return "姓名:杨茜，学号42411114，班级：人工智能";
    }

    @PostMapping("/attendance")
    public String studentAttendance(@RequestBody String studentId) {
        return "学号为 " + studentId + " 的学生打卡成功!";
    }

    @GetMapping("/courses")
    public List<String> getCourses() {
        return Arrays.asList("机器学习与数据挖掘", "数据库原理与应用");
    }

    // 任务一：路径参数查询单个学生
    @GetMapping("/info/{id}")
    public Result<Student> getStudentById(@PathVariable String id) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return Result.error("学生不存在");
        }
        return Result.success(student);
    }

    // 任务二：查询参数查询学生列表
    @GetMapping("/list")
    public Result<List<Student>> searchStudent(
            @RequestParam String className,
            @RequestParam(defaultValue = "1") int page) {

        List<Student> students = studentService.getStudentsByClassName(className);
        return Result.success(students);
    }

    // 任务三：JSON 体参数更新考勤记录
    @PostMapping("/attendance/update")
    public Result<String> updateAttendance(@RequestBody AttendanceRecord record) {
        String result = String.format("更新学生[%s]在[%s]的考勤状态为：%s",
                record.getStudentId(), record.getDate(), record.getStatus());
        return Result.success(result);
    }

    // ===================== Service 接口 =======================
    @PostMapping("/create")
    public Result<String> create(@RequestBody Student student) {
        return Result.success(studentService.createStudent(student));
    }

    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable String id) {
        return Result.success(studentService.getStudentById(id));
    }

    // 更新学生信息
    @PutMapping("/update")
    public Result<String> update(@RequestBody Student student) {
        String result = studentService.updateStudent(student);
        if ("学生不存在".equals(result)) {
            return Result.error(result);
        }
        return Result.success(result);
    }

    // 删除学生
    @DeleteMapping("/{studentId}")
    public Result<String> delete(@PathVariable String studentId) {
        String result = studentService.deleteStudent(studentId);
        if ("学生不存在".equals(result)) {
            return Result.error(result);
        }
        return Result.success(result);
    }

    // 分页查询所有学生
    @GetMapping("/page")
    public Result<Page<Student>> getStudentPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentId") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Student> studentPage = studentService.getStudentPage(pageRequest);
        return Result.success(studentPage);
    }

    // 根据班级分页查询学生
    @GetMapping("/page/class")
    public Result<Page<Student>> getStudentPageByClassName(
            @RequestParam String className,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentId") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<Student> studentPage = studentService.getStudentPageByClassName(className, pageRequest);
        return Result.success(studentPage);
    }

    // 查询所有学生列表
    @GetMapping("/all")
    public Result<List<Student>> getAllStudents() {
        return Result.success(studentService.getAllStudents());
    }

    // 获取所有班级列表
    @GetMapping("/classes")
    public Result<List<String>> getAllClasses() {
        return Result.success(studentRepository.findAllClassNames());
    }

    // 获取教师课程涉及的班级列表
    @GetMapping("/teacher-classes")
    public Result<List<String>> getTeacherClasses(@RequestParam String username) {
        List<String> classNames = courseSelectionRepository.findDistinctClassNamesByCourseIds(
            courseRepository.findByTeacherId(username).stream()
                .map(c -> c.getCourseId())
                .toList()
        );
        return Result.success(classNames);
    }
}