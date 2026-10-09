package ru.ulstu.file_service.domain;

import java.time.Instant;

public record StoredFileInfo(
        String id,
        String originalName,
        long size,
        Instant storedAt
) {}