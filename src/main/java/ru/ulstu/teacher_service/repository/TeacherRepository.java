package ru.ulstu.teacher_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.teacher_service.domain.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    long countByStatus_Code(String code);
}