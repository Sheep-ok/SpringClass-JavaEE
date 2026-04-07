package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import com.example.attendance.util.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 * 功能：用户的新增、查询、更新、删除等接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 构造注入
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 新增用户
     * 方式：POST
     * 输入：username, userpassword, realName, userrole
     */
    @PostMapping("/add")
    public Result<String> addUser(@RequestBody User user) {
        userService.addUser(user);
        return Result.success("用户添加成功！");
    }

    /**
     * 根据用户ID查询用户
     * 方式：GET
     * 输入示例：/user/1
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 根据用户名查询用户（用于登录）
     * 请求方式：GET
     * 输入示例：/user/username/admin
     */
    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return Result.success(user);
    }

    /**
     * 查询所有用户
     * 请求方式：GET
     * 无输入
     */
    @GetMapping("/list")
    public Result<List<User>> getAllUsers() {
        List<User> list = userService.getAllUsers();
        return Result.success(list);
    }

    /**
     * 更新用户信息
     * 请求方式：PUT
     * 输入：id, userpassword, realName, userrole
     */
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        userService.updateUser(user);
        return Result.success("用户更新成功！");
    }

    /**
     * 根据ID删除用户
     * 方式：DELETE
     * 输入示例：/user/1
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户删除成功！");
    }
}