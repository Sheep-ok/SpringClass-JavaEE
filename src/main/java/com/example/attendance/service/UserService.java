package com.example.attendance.service;

import com.example.attendance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    void addUser(User user);
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(Long id);
    Page<User> getUserPage(Pageable pageable);
    Page<User> getUserPageByRole(String role, Pageable pageable);
}