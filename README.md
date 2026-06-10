# SpringClass-JavaEE:for my classwork and homework
## 姓名：杨茜
### 学号：42411114

# 课堂考勤管理系统

## 项目简介
课堂考勤管理系统是一个基于Spring Boot开发的Web应用，用于高效管理学生课堂考勤记录，支持学生、教师、管理员三种角色的差异化操作。

## 技术栈
- **后端**：Spring Boot 4.0.3, Spring Security, Spring Data JPA
- **前端**：Thymeleaf, Bootstrap 5, ECharts
- **数据库**：MySQL 8.0+
- **构建工具**：Maven
- **Excel处理**：Apache POI

## 功能特性
- 用户注册与登录（支持学生、教师、管理员三种角色）
- 学生信息管理（新增、编辑、删除、分页查询）
- 课程管理（课程信息维护、选课管理）
- 考勤打卡功能（学生单点签到、教师批量考勤）
- 考勤记录查询与统计（多条件筛选、分页展示）
- 批量导入考勤数据（Excel文件上传与预览校验）
- 数据统计可视化（饼图、折线图、柱状图）

## 快速开始

### 环境要求
- JDK 21+
- Maven 3.6+
- MySQL 8.0+

### 运行步骤
1. 克隆项目到本地
2. 配置数据库连接信息（application.yml）
3. 启动项目：`mvn spring-boot:run`
4. 访问 http://localhost:8080 进入系统

## 联系方式
- 邮箱：2875337485@example.com