package ru.ulstu.teacher_service.web.dto;

import java.time.Instant;

public record StoredFileInfoDto(
        String id,
        String originalName,
        long size,
        Instant storedAt
) {
}