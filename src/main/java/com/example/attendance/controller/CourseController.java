package com.example.attendance.controller;

import com.example.attendance.entity.Attendance;
import com.example.attendance.entity.Course;
import com.example.attendance.entity.CourseSelection;
import com.example.attendance.entity.User;
import com.example.attendance.repository.AttendanceRepository;
import com.example.attendance.repository.CourseRepository;
import com.example.attendance.repository.CourseSelectionRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.entity.Student;
import com.example.attendance.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class CourseController {

    private final CourseRepository courseRepository;
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final CourseSelectionRepository courseSelectionRepository;
    private final StudentRepository studentRepository;

    public CourseController(CourseRepository courseRepository,
                            AttendanceRepository attendanceRepository,
                            UserRepository userRepository,
                            CourseSelectionRepository courseSelectionRepository,
                            StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.courseSelectionRepository = courseSelectionRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/courses")
    public Result<Map<String, Object>> getCourses(@RequestParam(required = false) String username) {
        Map<String, Object> result = new HashMap<>();

        if (username == null || username.isEmpty()) {
            return Result.error("用户名不能为空");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Result.error("用户不存在");
        }

        User user = userOpt.get();
        String role = user.getUserrole();
        String realName = user.getRealName();

        List<Course> courses = new ArrayList<>();

        switch (role.toUpperCase()) {
            case "STUDENT":
                List<CourseSelection> selections = courseSelectionRepository.findByStudentIdAndStatus(username, 1);
                for (CourseSelection selection : selections) {
                    courseRepository.findByCourseId(selection.getCourseId()).ifPresent(courses::add);
                }
                result.put("type", "student");
                result.put("className", realName);
                break;
            case "TEACHER":
                courses = courseRepository.findByTeacherId(username);
                result.put("type", "teacher");
                result.put("teacherName", realName);
                break;
            case "ADMIN":
                courses = courseRepository.findAll();
                result.put("type", "admin");
                break;
            default:
                result.put("type", "unknown");
        }

        result.put("courses", courses);
        result.put("role", role);
        result.put("userName", realName);

        List<Map<String, Object>> attendanceStats = new ArrayList<>();

        if ("ADMIN".equals(role.toUpperCase())) {
            long studentCount = userRepository.countByUserroleIgnoreCase("STUDENT") + userRepository.countByUserroleIgnoreCase("USER");
            long teacherCount = userRepository.countByUserroleIgnoreCase("TEACHER");
            long courseCount = courseRepository.count();

            Map<String, Object> stat = new HashMap<>();
            stat.put("present", studentCount);
            stat.put("absent", teacherCount);
            stat.put("rate", courseCount);
            attendanceStats.add(stat);

            for (Course course : courses) {
                Map<String, Object> courseStat = new HashMap<>();
                courseStat.put("courseId", course.getCourseId());
                courseStat.put("courseName", course.getCourseName());

                List<Attendance> attendanceList = attendanceRepository.findByCourseId(course.getCourseId());
                int total = attendanceList.size();
                long present = attendanceList.stream().filter(a -> a.getStudentStatus() == 1).count();
                long absent = attendanceList.stream().filter(a -> a.getStudentStatus() != 1).count();

                courseStat.put("total", total);
                courseStat.put("present", present);
                courseStat.put("absent", absent);
                courseStat.put("rate", total > 0 ? String.format("%.1f", (present * 100.0 / total)) : "0.0");

                attendanceStats.add(courseStat);
            }
        } else if ("TEACHER".equals(role.toUpperCase())) {
            int courseCount = courses.size();
            long totalPresent = 0;
            long totalAbsent = 0;
            long totalStudents = studentRepository.count();

            for (Course course : courses) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("courseId", course.getCourseId());
                stat.put("courseName", course.getCourseName());

                List<Attendance> attendanceList = attendanceRepository.findByCourseId(course.getCourseId());
                int total = attendanceList.size();
                long present = attendanceList.stream().filter(a -> a.getStudentStatus() == 1).count();
                long absent = attendanceList.stream().filter(a -> a.getStudentStatus() != 1).count();

                stat.put("total", total);
                stat.put("present", present);
                stat.put("absent", absent);
                stat.put("rate", total > 0 ? String.format("%.1f", (present * 100.0 / total)) : "0.0");

                attendanceStats.add(stat);

                totalPresent += present;
                totalAbsent += absent;
            }

            long totalRecords = totalPresent + totalAbsent;
            String overallRate = totalRecords > 0 ? String.format("%.1f", (totalPresent * 100.0 / totalRecords)) : "0.0";

            Map<String, Object> summaryStat = new HashMap<>();
            summaryStat.put("present", totalStudents);
            summaryStat.put("absent", courseCount);
            summaryStat.put("rate", overallRate);
            attendanceStats.add(0, summaryStat);
        } else if ("STUDENT".equals(role.toUpperCase())) {
            List<CourseSelection> selections = courseSelectionRepository.findByStudentIdAndStatus(username, 1);
            long courseCount = selections.stream().map(CourseSelection::getCourseId).distinct().count();

            List<Attendance> studentAttendance = attendanceRepository.findByStudentId(username);
            int total = studentAttendance.size();
            long present = studentAttendance.stream().filter(a -> a.getStudentStatus() == 1).count();
            long absent = studentAttendance.stream().filter(a -> a.getStudentStatus() != 1).count();

            Map<String, Object> stat = new HashMap<>();
            stat.put("total", total);
            stat.put("present", courseCount);
            stat.put("absent", present);
            stat.put("rate", total > 0 ? String.format("%.1f", (present * 100.0 / total)) : "0.0");
            attendanceStats.add(stat);
        }

        result.put("attendanceStats", attendanceStats);

        return Result.success(result);
    }

    @GetMapping("/courses/all")
    public Result<List<Course>> getAllCourses() {
        return Result.success(courseRepository.findAll());
    }

    @PostMapping("/courses")
    public Result<String> addCourse(@RequestBody Course course) {
        if (courseRepository.findByCourseId(course.getCourseId()).isPresent()) {
            return Result.error("课程ID已存在");
        }
        courseRepository.save(course);
        return Result.success("课程添加成功");
    }

    @PutMapping("/courses")
    public Result<String> updateCourse(@RequestBody Course course) {
        if (courseRepository.findByCourseId(course.getCourseId()).isEmpty()) {
            return Result.error("课程不存在");
        }
        courseRepository.save(course);
        return Result.success("课程更新成功");
    }

    @DeleteMapping("/courses/{courseId}")
    public Result<String> deleteCourse(@PathVariable String courseId) {
        Optional<Course> course = courseRepository.findByCourseId(courseId);
        if (course.isEmpty()) {
            return Result.error("课程不存在");
        }
        courseRepository.delete(course.get());
        return Result.success("课程删除成功");
    }

    @PostMapping("/course-selection")
    public Result<String> addCourseSelection(@RequestBody CourseSelection selection) {
        if (courseSelectionRepository.existsByStudentIdAndCourseId(selection.getStudentId(), selection.getCourseId())) {
            return Result.error("已选该课程");
        }
        
        Optional<Course> course = courseRepository.findByCourseId(selection.getCourseId());
        if (course.isPresent()) {
            selection.setCourseName(course.get().getCourseName());
            selection.setClassName(course.get().getClassName());
        }
        
        courseSelectionRepository.save(selection);
        return Result.success("选课成功");
    }

    @DeleteMapping("/course-selection/{id}")
    public Result<String> deleteCourseSelection(@PathVariable Long id) {
        if (!courseSelectionRepository.existsById(id)) {
            return Result.error("选课记录不存在");
        }
        courseSelectionRepository.deleteById(id);
        return Result.success("退课成功");
    }

    @GetMapping("/course-selection/student/{studentId}")
    public Result<List<CourseSelection>> getStudentSelections(@PathVariable String studentId) {
        return Result.success(courseSelectionRepository.findByStudentIdAndStatus(studentId, 1));
    }

    @GetMapping("/course-selection/course/{courseId}")
    public Result<List<CourseSelection>> getCourseSelections(@PathVariable String courseId) {
        return Result.success(courseSelectionRepository.findByCourseIdAndStatus(courseId, 1));
    }

    @GetMapping("/students")
    public Result<Map<String, Object>> getStudentsByCourse(@RequestParam(required = false) String courseId) {
        Map<String, Object> result = new HashMap<>();
        
        if (courseId != null && !courseId.isEmpty()) {
            List<CourseSelection> selections = courseSelectionRepository.findByCourseIdAndStatus(courseId, 1);
            List<Student> students = new ArrayList<>();
            
            for (CourseSelection selection : selections) {
                Student student = studentRepository.findById(selection.getStudentId()).orElse(null);
                if (student != null) {
                    students.add(student);
                }
            }
            
            result.put("students", students);
        } else {
            result.put("students", studentRepository.findAll());
        }
        
        return Result.success(result);
    }
}