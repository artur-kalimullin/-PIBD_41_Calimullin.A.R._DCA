package ru.ulstu.report_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ulstu.report_service.domain.ReportSummary;
import ru.ulstu.report_service.domain.TeacherReportRow;
import ru.ulstu.report_service.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Отчёты", description = "Сервис отчётов по преподавателям")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/teachers")
    @Operation(summary = "Список преподавателей для отчёта")
    public List<TeacherReportRow> teachers() {
        log.info("REPORT: incoming GET /api/reports/teachers");
        List<TeacherReportRow> result = service.findAllTeachers();
        log.info("REPORT: returning {} rows", result.size());
        return result;
    }

    @GetMapping("/summary")
    @Operation(summary = "Сводка: сколько работает, сколько уволено")
    public ReportSummary summary() {
        log.info("REPORT: incoming GET /api/reports/summary");
        ReportSummary result = service.buildSummary();
        log.info("REPORT: returning summary working={} fired={}",
                result.workingCount(), result.firedCount());
        return result;
    }
}