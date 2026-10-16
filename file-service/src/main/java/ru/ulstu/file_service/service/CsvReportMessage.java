package ru.ulstu.file_service.service;

public record CsvReportMessage(
        String filename,
        String contentBase64
) {}