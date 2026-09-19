package ru.ulstu.teacher_service.web.dto;

import ru.ulstu.teacher_service.domain.Teacher;

import java.time.Instant;

public record TeacherDto(
        Long id,
        String fullName,
        String position,
        String statusCode,   // WORKING / FIRED
        String statusName,   // Работает / Уволен
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
                teacher.getStatus().getCode(),
                teacher.getStatus().getName(),
                teacher.getFireReason(),
                teacher.getSubject(),
                teacher.getHiredAt(),
                teacher.getFiredAt(),
                teacher.getCreatedAt());
    }
}