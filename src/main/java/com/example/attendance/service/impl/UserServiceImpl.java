package com.example.attendance.service.impl;

import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public UserServiceImpl(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public void addUser(User user) {
        user.setCreateTime(LocalDateTime.now());
        if (user.getGender() == null || user.getGender().isEmpty()) {
            user.setGender(Math.random() > 0.5 ? "男" : "女");
        }
        userRepository.save(user);

        if ("STUDENT".equals(user.getUserrole()) || "USER".equals(user.getUserrole())) {
            Student student = new Student();
            student.setStudentId(user.getUsername());
            student.setName(user.getRealName() != null && !user.getRealName().isEmpty() ? user.getRealName() : user.getUsername());
            student.setGender(user.getGender());
            student.setClassName("未分配");
            student.setAge(20);
            studentRepository.save(student);
        }
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void updateUser(User user) {
        User existingUser = userRepository.findById(user.getId()).orElse(null);
        if (existingUser != null) {
            if (user.getUserpassword() == null || user.getUserpassword().isEmpty()) {
                user.setUserpassword(existingUser.getUserpassword());
            }
            if (user.getCreateTime() == null) {
                user.setCreateTime(existingUser.getCreateTime());
            }
            if (user.getGender() == null || user.getGender().isEmpty()) {
                user.setGender(existingUser.getGender());
            }
        }
        userRepository.save(user);

        if ("STUDENT".equals(user.getUserrole()) || "USER".equals(user.getUserrole())) {
            if (studentRepository.findByStudentId(user.getUsername()).orElse(null) == null) {
                Student student = new Student();
                student.setStudentId(user.getUsername());
                student.setName(user.getRealName() != null && !user.getRealName().isEmpty() ? user.getRealName() : user.getUsername());
                student.setGender(user.getGender());
                student.setClassName("未分配");
                student.setAge(20);
                studentRepository.save(student);
            }
        }
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Page<User> getUserPage(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> getUserPageByRole(String role, Pageable pageable) {
        return userRepository.findByUserrole(role, pageable);
    }
}