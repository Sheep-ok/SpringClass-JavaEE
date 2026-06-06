package com.example.attendance.controller;

import com.example.attendance.entity.Student;
import com.example.attendance.entity.User;
import com.example.attendance.repository.StudentRepository;
import com.example.attendance.service.UserService;
import com.example.attendance.util.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, StudentRepository studentRepository,
                         PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/add")
    public Result<String> addUser(@RequestBody User user) {
        userService.addUser(user);
        return Result.success("用户添加成功！");
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return Result.success(user);
    }

    @GetMapping("/list")
    public Result<List<User>> getAllUsers() {
        List<User> list = userService.getAllUsers();
        return Result.success(list);
    }

    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return Result.success("用户更新成功！");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户删除成功！");
    }

    @GetMapping("/page")
    public Result<Page<User>> getUserPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<User> userPage = userService.getUserPage(pageRequest);
        return Result.success(userPage);
    }

    @GetMapping("/page/role")
    public Result<Page<User>> getUserPageByRole(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<User> userPage = userService.getUserPageByRole(role, pageRequest);
        return Result.success(userPage);
    }

    @GetMapping("/profile/{username}")
    public Result<Map<String, Object>> getProfile(@PathVariable String username) {
        Optional<Student> studentOpt = studentRepository.findByStudentId(username);
        if (studentOpt.isEmpty()) {
            return Result.error("学生信息不存在");
        }

        Student student = studentOpt.get();
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("studentId", student.getStudentId());
        if (student.getName() != null && !student.getName().isEmpty()) {
            profile.put("name", student.getName());
        }
        if (student.getGender() != null && !student.getGender().isEmpty()) {
            profile.put("gender", student.getGender());
        }
        if (student.getClassName() != null && !student.getClassName().isEmpty()) {
            profile.put("className", student.getClassName());
        }
        if (student.getAge() != null) {
            profile.put("age", student.getAge());
        }

        return Result.success(profile);
    }

    @PutMapping("/profile/password")
    public Result<String> changePassword(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (username == null || oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        User user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getUserpassword())) {
            return Result.error("旧密码不正确");
        }

        user.setUserpassword(passwordEncoder.encode(newPassword));
        userService.updateUser(user);

        return Result.success("密码修改成功");
    }
}