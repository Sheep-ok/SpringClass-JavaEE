package com.example.attendance.service.impl;

import com.example.attendance.entity.Student;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public String createStudent(Student student) {
        if (student.getGender() == null || student.getGender().isEmpty()) {
            student.setGender(Math.random() > 0.5 ? "男" : "女");
        }
        studentRepository.save(student);
        return "学生创建成功";
    }

    @Override
    public Student getStudentById(String id) {
        return studentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public List<Student> getStudentsByClassName(String className) {
        return studentRepository.findByClassName(className);
    }

    @Override
    public String updateStudent(Student student) {
        if (!studentRepository.existsById(student.getStudentId())) {
            return "学生不存在";
        }
        studentRepository.save(student);
        return "学生更新成功";
    }

    @Override
    public String deleteStudent(String studentId) {
        if (!studentRepository.existsById(studentId)) {
            return "学生不存在";
        }
        studentRepository.deleteById(studentId);
        return "学生删除成功";
    }

    @Override
    public Page<Student> getStudentPage(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    @Override
    public Page<Student> getStudentPageByClassName(String className, Pageable pageable) {
        return studentRepository.findByClassName(className, pageable);
    }
}