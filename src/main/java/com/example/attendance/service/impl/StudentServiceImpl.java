package com.example.attendance.service.impl;

import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public String createStudent(Student student) {
        logger.info("开始创建学生，学生ID: {}", student.getStudentId());
        if (student.getGender() == null || student.getGender().isEmpty()) {
            student.setGender(Math.random() > 0.5 ? "男" : "女");
            logger.debug("学生性别为空，自动设置为: {}", student.getGender());
        }
        studentRepository.save(student);
        logger.info("学生创建成功，学生ID: {}", student.getStudentId());
        return "学生创建成功";
    }

    @Override
    public Student getStudentById(String id) {
        logger.debug("查询学生信息，学生ID: {}", id);
        Student student = studentRepository.findById(id).orElse(null);
        if (student != null) {
            logger.debug("查询到学生信息，学生ID: {}, 姓名: {}", id, student.getName());
        } else {
            logger.debug("未找到学生信息，学生ID: {}", id);
        }
        return student;
    }

    @Override
    public List<Student> getAllStudents() {
        logger.debug("查询所有学生列表");
        List<Student> students = studentRepository.findAll();
        logger.info("查询到 {} 名学生", students.size());
        return students;
    }

    @Override
    public List<Student> getStudentsByClassName(String className) {
        logger.debug("按班级查询学生，班级名称: {}", className);
        List<Student> students = studentRepository.findByClassName(className);
        logger.info("班级 {} 查询到 {} 名学生", className, students.size());
        return students;
    }

    @Override
    public String updateStudent(Student student) {
        logger.info("开始更新学生信息，学生ID: {}", student.getStudentId());
        if (!studentRepository.existsById(student.getStudentId())) {
            logger.warn("更新学生失败，学生不存在，学生ID: {}", student.getStudentId());
            return "学生不存在";
        }
        studentRepository.save(student);
        logger.info("学生更新成功，学生ID: {}", student.getStudentId());
        return "学生更新成功";
    }

    @Override
    public String deleteStudent(String studentId) {
        logger.info("开始删除学生，学生ID: {}", studentId);
        if (!studentRepository.existsById(studentId)) {
            logger.warn("删除学生失败，学生不存在，学生ID: {}", studentId);
            return "学生不存在";
        }
        studentRepository.deleteById(studentId);
        logger.info("学生删除成功，学生ID: {}", studentId);
        return "学生删除成功";
    }

    @Override
    public Page<Student> getStudentPage(Pageable pageable) {
        logger.debug("分页查询学生列表，页码: {}, 每页数量: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Student> page = studentRepository.findAll(pageable);
        logger.info("分页查询完成，总数: {}, 当前页数量: {}", page.getTotalElements(), page.getNumberOfElements());
        return page;
    }

    @Override
    public Page<Student> getStudentPageByClassName(String className, Pageable pageable) {
        logger.debug("按班级分页查询学生，班级名称: {}, 页码: {}", className, pageable.getPageNumber());
        Page<Student> page = studentRepository.findByClassName(className, pageable);
        logger.info("班级 {} 分页查询完成，总数: {}", className, page.getTotalElements());
        return page;
    }
}