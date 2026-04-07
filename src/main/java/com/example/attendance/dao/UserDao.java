package com.example.attendance.dao;

import com.example.attendance.entity.User;
import java.util.List;

public interface UserDao {
    // 新增用户
    void insert(User user);

    // 根据ID查询
    User findById(Long id);
    // 根据用户名查询,用于登录验证
    User findByUsername(String username);

    // 查询所有用户
    List<User> findAll();

    // 更新用户
    void update(User user);
    // 删除用户
    void deleteById(Long id);

}