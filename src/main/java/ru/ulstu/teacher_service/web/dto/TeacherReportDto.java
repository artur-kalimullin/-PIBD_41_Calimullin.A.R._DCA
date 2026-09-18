package ru.ulstu.teacher_service.web.dto;

public record TeacherReportDto(
        long workingCount,
        long firedCount,
        long total) {
}