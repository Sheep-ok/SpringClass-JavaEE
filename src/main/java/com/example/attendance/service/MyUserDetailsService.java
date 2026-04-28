package com.example.attendance.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface MyUserDetailsService extends UserDetailsService {
    @Override
    org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}