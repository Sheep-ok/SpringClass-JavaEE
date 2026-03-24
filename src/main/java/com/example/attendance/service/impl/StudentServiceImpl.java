package com.example.attendance.service.impl;

import com.example.attendance.dao.StudentDao;
import com.example.attendance.entity.Student;
import com.example.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service // 标记为Service层组件，由Spring管理
public class StudentServiceImpl implements StudentService {

    @Autowired // 自动注入Dao层依赖
    private StudentDao studentDao;

    @Override
    public String createStudent(Student student) {
        // 业务校验：姓名不能为空
        if (student.getName() == null || student.getName().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        // 调用Dao层执行数据库操作
        studentDao.insert(student);
        return "创建成功";
    }

    @Override
    public Student getStudentById(String studentId) {
        // 调用Dao层查询数据
        return studentDao.findById(studentId);
    }
}