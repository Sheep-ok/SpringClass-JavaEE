package com.example.attendance.controller;

import com.example.attendance.entity.User;
import com.example.attendance.service.UserService;
import com.example.attendance.util.Result;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
}