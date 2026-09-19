package ru.ulstu.teacher_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ulstu.teacher_service.domain.TeacherStatus;

import java.util.Optional;

public interface TeacherStatusRepository extends JpaRepository<TeacherStatus, Long> {
    Optional<TeacherStatus> findByCode(String code);
}