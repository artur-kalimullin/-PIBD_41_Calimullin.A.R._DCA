package ru.ulstu.report_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.ulstu.report_service.domain.ReportSummary;
import ru.ulstu.report_service.domain.TeacherReportRow;

import java.util.List;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final RestTemplate restTemplate;
    private final String teacherServiceUrl;

    public ReportService(RestTemplate restTemplate,
                         @Value("${teacher.service.url}") String teacherServiceUrl) {
        this.restTemplate = restTemplate;
        this.teacherServiceUrl = teacherServiceUrl;
    }

    public List<TeacherReportRow> findAllTeachers() {
        String url = teacherServiceUrl + "/api/teachers/all";
        log.info("REPORT -> teacher-service: GET {}", url);
        ResponseEntity<List<TeacherReportRow>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TeacherReportRow>>() {}
        );
        List<TeacherReportRow> body = response.getBody();
        int size = body == null ? 0 : body.size();
        log.info("REPORT <- teacher-service: status={} rows={}",
                response.getStatusCode().value(), size);
        return body;
    }

    public ReportSummary buildSummary() {
        log.info("REPORT: building summary");
        List<TeacherReportRow> teachers = findAllTeachers();
        long working = teachers == null ? 0 : teachers.stream()
                .filter(t -> "WORKING".equals(t.statusCode()))
                .count();
        long fired = teachers == null ? 0 : teachers.stream()
                .filter(t -> "FIRED".equals(t.statusCode()))
                .count();
        log.info("REPORT: summary built working={} fired={}", working, fired);
        return new ReportSummary(working, fired);
    }
}