package com.example.attendance.repository;

import com.example.attendance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByUserrole(String userrole);
    List<User> findByRealNameContaining(String keyword);
    long countByUserrole(String userrole);
    long countByUserroleIgnoreCase(String userrole);
    boolean existsByUsername(String username);
    Page<User> findByUserrole(String userrole, Pageable pageable);
}