package ru.ulstu.report_service.domain;

import java.time.Instant;

public record TeacherReportRow(
        Long id,
        String fullName,
        String position,
        String statusCode,
        String statusName,
        String fireReason,
        String subject,
        Instant hiredAt,
        Instant firedAt,
        Instant createdAt
) {
}