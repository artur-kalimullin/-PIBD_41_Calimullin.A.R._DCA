package ru.ulstu.teacher_service.web.dto;

import ru.ulstu.teacher_service.domain.Teacher;
import ru.ulstu.teacher_service.domain.TeacherStatus;

import java.time.Instant;

public record TeacherDto(
        Long id,
        String fullName,
        String position,
        TeacherStatus status,
        String fireReason,
        String subject,
        Instant hiredAt,
        Instant firedAt,
        Instant createdAt) {

    public static TeacherDto of(Teacher teacher) {
        return new TeacherDto(
                teacher.getId(),
                teacher.getFullName(),
                teacher.getPosition(),
                teacher.getStatus(),
                teacher.getFireReason(),
                teacher.getSubject(),
                teacher.getHiredAt(),
                teacher.getFiredAt(),
                teacher.getCreatedAt());
    }
}