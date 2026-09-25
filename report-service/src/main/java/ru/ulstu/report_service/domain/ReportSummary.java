package ru.ulstu.report_service.domain;

public record ReportSummary(
        long workingCount,
        long firedCount
) {
}