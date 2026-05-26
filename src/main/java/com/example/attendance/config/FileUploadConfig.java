package com.example.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    // 最大文件大小限制 (10MB)
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    // 允许的文件扩展名
    public static final String[] ALLOWED_EXTENSIONS = {".xls", ".xlsx"};
    
    // 允许的MIME类型
    public static final String[] ALLOWED_MIME_TYPES = {
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}