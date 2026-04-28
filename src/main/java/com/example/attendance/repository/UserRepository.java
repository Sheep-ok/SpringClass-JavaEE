package com.example.attendance.repository;

import com.example.attendance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 1. 根据用户名查询
    Optional<User> findByUsername(String username);

    // 2. 根据角色查询用户（如ADMIN/TEACHER/STUDENT）
    List<User> findByUserrole(String userrole);

    // 3. 根据真实姓名模糊查询
    List<User> findByRealNameContaining(String keyword);

    // 4. 统计某角色用户数量
    long countByUserrole(String userrole);

    boolean existsByUsername(String username);
}