package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.util.Result;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(user.getUsername())) {
            return Result.error("用户名已存在");
        }

        // 密码加密
        user.setUserpassword(passwordEncoder.encode(user.getUserpassword()));

        // 时间设为 null，让数据库自动生成
        user.setCreateTime(null);

        // 保存
        userRepository.save(user);

        return Result.success("注册成功");
    }
}