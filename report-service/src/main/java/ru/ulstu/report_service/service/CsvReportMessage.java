package ru.ulstu.report_service.service;

public record CsvReportMessage(
        String filename,
        String contentBase64
) {}