package ru.ulstu.report_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/teachers")
    @Operation(summary = "Список преподавателей для отчёта")
    public List<TeacherReportRow> teachers() {
        return service.findAllTeachers();
    }

    @GetMapping("/summary")
    @Operation(summary = "Сводка: сколько работает, сколько уволено")
    public ReportSummary summary() {
        return service.buildSummary();
    }
}