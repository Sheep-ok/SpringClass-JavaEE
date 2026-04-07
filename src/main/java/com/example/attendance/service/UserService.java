package com.example.attendance.service;
import com.example.attendance.entity.User;
import java.util.List;

public interface UserService {
    void addUser(User user);
    User getUserById(Long id);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(Long id);
}
