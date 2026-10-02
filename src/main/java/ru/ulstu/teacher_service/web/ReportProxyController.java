package ru.ulstu.teacher_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Отчёты", description = "Проксирование запросов к сервису отчётов")
public class ReportProxyController {

    private final RestTemplate restTemplate;
    private final String reportServiceUrl;

    public ReportProxyController(RestTemplate restTemplate,
                                 @Value("${report.service.url}") String reportServiceUrl) {
        this.restTemplate = restTemplate;
        this.reportServiceUrl = reportServiceUrl;
    }

    @GetMapping("/teachers")
    @Operation(summary = "Список преподавателей для отчёта")
    public ResponseEntity<Object> getTeachers() {
        String url = reportServiceUrl + "/api/reports/teachers";
        return restTemplate.getForEntity(url, Object.class);
    }

    @GetMapping("/summary")
    @Operation(summary = "Сводка: сколько работает, сколько уволено")
    public ResponseEntity<Object> getSummary() {
        String url = reportServiceUrl + "/api/reports/summary";
        return restTemplate.getForEntity(url, Object.class);
    }
}