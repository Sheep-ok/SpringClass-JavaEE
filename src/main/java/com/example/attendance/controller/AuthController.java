package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.repository.UserRepository;
import com.example.attendance.util.Result;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===================== 页面跳转（解决404核心）=====================
    @GetMapping("/")
    public String loginPage() {
        return "login"; // 访问 localhost:8080 直接进登录页
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // 注册页面
    }

    @GetMapping("/index")
    public String index() {
        return "index"; // 首页
    }

    // ===================== 注册接口（密码加密）=====================
    @PostMapping("/auth/register")
    @ResponseBody
    public Result<String> register(@RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return Result.error("用户名已存在");
        }

        // 密码加密
        String encodedPwd = passwordEncoder.encode(user.getUserpassword());
        user.setUserpassword(encodedPwd);
        user.setCreateTime(null);

        userRepository.save(user);
        return Result.success("注册成功");
    }

    // ===================== 登录接口 =====================
    @PostMapping("/auth/login")
    @ResponseBody
    public Result<User> login(@RequestBody User user) {
        User exist = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (exist == null) {
            return Result.error("用户不存在");
        }

        String rawPassword = user.getUserpassword();
        String encodedPassword = exist.getUserpassword();

        boolean matches = false;

        if (passwordEncoder.matches(rawPassword, encodedPassword)) {
            matches = true;
        } else if (rawPassword.equals(encodedPassword)) {
            matches = true;
            exist.setUserpassword(passwordEncoder.encode(rawPassword));
            userRepository.save(exist);
        }

        if (!matches) {
            return Result.error("密码错误");
        }

        exist.setUserpassword(null);
        return Result.success(exist);
    }
}