package com.example.attendance.dao;

import com.example.attendance.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;



@Repository
public class StudentDao
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insert(Student student) {
        String sql = "INSERT INTO student (student_id, name, class_name) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, student.getStudentId(), student.getName(), student.getClassName());
    }

    public Student findById(String studentId) {
        String sql = "SELECT * FROM student WHERE student_id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Student.class), studentId);
    }
}
