package ru.ulstu.file_service.service;

public record FileSaveFailedEvent(
        String filename,
        String contentBase64,
        String reason
) {}