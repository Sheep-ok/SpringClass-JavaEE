package com.example.attendance.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String userpassword;
    private String realName;
    private String userrole;
    private LocalDateTime createTime;
}