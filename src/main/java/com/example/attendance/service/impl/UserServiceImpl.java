package com.example.attendance.service.impl;

import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public UserServiceImpl(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public void addUser(User user) {
        logger.info("开始添加用户，用户名: {}, 角色: {}", user.getUsername(), user.getUserrole());
        user.setCreateTime(LocalDateTime.now());
        if (user.getGender() == null || user.getGender().isEmpty()) {
            user.setGender(Math.random() > 0.5 ? "男" : "女");
            logger.debug("用户性别为空，自动设置为: {}", user.getGender());
        }
        userRepository.save(user);

        if ("STUDENT".equals(user.getUserrole()) || "USER".equals(user.getUserrole())) {
            Student student = new Student();
            student.setStudentId(user.getUsername());
            student.setName(user.getRealName() != null && !user.getRealName().isEmpty() ? user.getRealName() : user.getUsername());
            student.setGender(user.getGender());
            student.setClassName(user.getClassName() != null && !user.getClassName().isEmpty() ? user.getClassName() : "未分配");
            student.setAge(20);
            studentRepository.save(student);
            logger.info("用户关联学生信息创建成功，学生ID: {}", user.getUsername());
        }
        logger.info("用户添加成功，用户名: {}", user.getUsername());
    }

    @Override
    public User getUserById(Long id) {
        logger.debug("查询用户信息，用户ID: {}", id);
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            logger.debug("查询到用户信息，用户ID: {}, 用户名: {}", id, user.getUsername());
        } else {
            logger.debug("未找到用户信息，用户ID: {}", id);
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        logger.debug("按用户名查询用户，用户名: {}", username);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            logger.debug("查询到用户，用户名: {}, 角色: {}", username, user.getUserrole());
        } else {
            logger.debug("未找到用户，用户名: {}", username);
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        logger.debug("查询所有用户列表");
        List<User> users = userRepository.findAll();
        logger.info("查询到 {} 个用户", users.size());
        return users;
    }

    @Override
    public void updateUser(User user) {
        logger.info("开始更新用户信息，用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
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
            Student existingStudent = studentRepository.findByStudentId(user.getUsername()).orElse(null);
            if (existingStudent == null) {
                Student student = new Student();
                student.setStudentId(user.getUsername());
                student.setName(user.getRealName() != null && !user.getRealName().isEmpty() ? user.getRealName() : user.getUsername());
                student.setGender(user.getGender());
                student.setClassName(user.getClassName() != null && !user.getClassName().isEmpty() ? user.getClassName() : "未分配");
                student.setAge(20);
                studentRepository.save(student);
                logger.info("用户关联学生信息创建成功，学生ID: {}", user.getUsername());
            } else {
                existingStudent.setName(user.getRealName() != null && !user.getRealName().isEmpty() ? user.getRealName() : user.getUsername());
                existingStudent.setGender(user.getGender());
                if (user.getClassName() != null && !user.getClassName().isEmpty()) {
                    existingStudent.setClassName(user.getClassName());
                }
                studentRepository.save(existingStudent);
                logger.info("用户关联学生信息更新成功，学生ID: {}", user.getUsername());
            }
        }
        logger.info("用户更新成功，用户名: {}", user.getUsername());
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("开始删除用户，用户ID: {}", id);
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            userRepository.deleteById(id);
            logger.info("用户删除成功，用户名: {}", user.getUsername());
        } else {
            logger.warn("删除用户失败，用户不存在，用户ID: {}", id);
        }
    }

    @Override
    public Page<User> getUserPage(Pageable pageable) {
        logger.debug("分页查询用户列表，页码: {}, 每页数量: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<User> page = userRepository.findAll(pageable);
        logger.info("分页查询完成，总数: {}", page.getTotalElements());
        return page;
    }

    @Override
    public Page<User> getUserPageByRole(String role, Pageable pageable) {
        logger.debug("按角色分页查询用户，角色: {}", role);
        Page<User> page = userRepository.findByUserrole(role, pageable);
        logger.info("角色 {} 查询完成，总数: {}", role, page.getTotalElements());
        return page;
    }
}