package ru.ulstu.teacher_service.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Отчёты", description = "Проксирование запросов к сервису отчётов")
public class ReportProxyController {

    private static final Logger log = LoggerFactory.getLogger(ReportProxyController.class);

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
        log.info("PROXY -> report-service: GET /api/reports/teachers");
        String url = reportServiceUrl + "/api/reports/teachers";
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        log.info("PROXY <- report-service: GET /api/reports/teachers status={}",
                response.getStatusCode().value());
        return response;
    }

    @GetMapping("/summary")
    @Operation(summary = "Сводка: сколько работает, сколько уволено")
    public ResponseEntity<Object> getSummary() {
        log.info("PROXY -> report-service: GET /api/reports/summary");
        String url = reportServiceUrl + "/api/reports/summary";
        ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
        log.info("PROXY <- report-service: GET /api/reports/summary status={} body={}",
                response.getStatusCode().value(), response.getBody());
        return response;
    }

    @PostMapping("/teachers/csv/save")
    @Operation(summary = "Прокси сохранения CSV-отчёта в файловое хранилище")
    public ResponseEntity<Object> saveCsv() {
        log.info("PROXY -> report-service: POST /api/reports/teachers/csv/save");
        String url = reportServiceUrl + "/api/reports/teachers/csv/save";
        ResponseEntity<Object> response = restTemplate.postForEntity(url, null, Object.class);
        log.info("PROXY <- report-service: status={}", response.getStatusCode().value());
        return response;
    }
}