package com.example.attendance.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "my_user") // 对应数据库user表
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 必须生成 getter/setter
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "userpassword", nullable = false, length = 100)
    private String userpassword;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "userrole", length = 20)
    private String userrole;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 3. 新增：保存前自动设置时间
    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}