package com.example.attendance.dao.impl;

import com.example.attendance.dao.UserDao;
import com.example.attendance.entity.User;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    // 构造器注入JdbcTemplate
    private final JdbcTemplate jdbcTemplate;
    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. 新增用户
    @Override
    public void insert(User user) {
        String sql = "INSERT INTO [user] (username, userpassword, real_name, userrole) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                user.getUsername(),
                user.getUserpassword(),
                user.getRealName(),
                user.getUserrole()
        );
    }

    // 2. 根据ID查询
    @Override
    public User findById(Long id) {
        String sql = "SELECT * FROM [user] WHERE id = ?";
        return jdbcTemplate.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(User.class),
                id
        );
    }

    // 3. 根据用户名查询
    @Override
    public User findByUsername(String username) {
        String sql = "SELECT * FROM [user] WHERE username = ?";
        return jdbcTemplate.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(User.class),
                username
        );
    }

    // 4. 查询所有用户
    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM [user]";
        return jdbcTemplate.query(
                sql,
                new BeanPropertyRowMapper<>(User.class)
        );
    }

    // 5. 更新用户
    @Override
    public void update(User user) {
        String sql = "UPDATE [user] SET userpassword = ?, real_name = ?, userrole = ? WHERE id = ?";
        jdbcTemplate.update(
                sql,
                user.getUserpassword(),
                user.getRealName(),
                user.getUserrole(),
                user.getId()
        );
    }

    // 6. 删除用户
    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM [user] WHERE id = ?";
        jdbcTemplate.update(
                sql,
                id
        );
    }
}