package com.example.attendance.repository;

import com.example.attendance.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {
    // 1. 根据学号查询学生（方法名规则自动生成SQL）
    Optional<Student> findByStudentId(String studentId);

    // 2. 根据班级查询学生列表
    List<Student> findByClassName(String className);

    // 3. 统计某班级学生数量（自定义查询）
    long countByClassName(String className);

    // 4. 根据班级分页查询
    Page<Student> findByClassName(String className, Pageable pageable);

    // 5. 根据姓名模糊查询
    List<Student> findByNameContaining(String name);

    // 6. 根据学号模糊查询
    List<Student> findByStudentIdContaining(String studentId);

    // 7. 获取所有班级名称
    @Query("SELECT DISTINCT s.className FROM Student s WHERE s.className IS NOT NULL ORDER BY s.className")
    List<String> findAllClassNames();
    
    // 8. 统计选了某门课程的学生数量
    @Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN CourseSelection cs ON s.studentId = cs.studentId WHERE cs.courseId = :courseId AND cs.status = 1")
    long countByCourseId(@Param("courseId") String courseId);
    
    // 9. 统计选了某门课程且属于某个班级的学生数量
    @Query("SELECT COUNT(DISTINCT s) FROM Student s JOIN CourseSelection cs ON s.studentId = cs.studentId WHERE cs.courseId = :courseId AND s.className = :className AND cs.status = 1")
    long countByCourseIdAndClassName(@Param("courseId") String courseId, @Param("className") String className);

    // 10. 根据课程ID列表查询学生（教师查看自己课程下的学生）
    @Query("SELECT DISTINCT s FROM Student s JOIN CourseSelection cs ON s.studentId = cs.studentId WHERE cs.courseId IN :courseIds AND cs.status = 1")
    List<Student> findStudentsByCourseIds(@Param("courseIds") List<String> courseIds);
}