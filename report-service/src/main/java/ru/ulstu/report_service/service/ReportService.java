package ru.ulstu.report_service.service;

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

    private final RestTemplate restTemplate;
    private final String teacherServiceUrl;

    public ReportService(RestTemplate restTemplate,
                         @Value("${teacher.service.url}") String teacherServiceUrl) {
        this.restTemplate = restTemplate;
        this.teacherServiceUrl = teacherServiceUrl;
    }

    public List<TeacherReportRow> findAllTeachers() {
        String url = teacherServiceUrl + "/api/teachers/all";
        ResponseEntity<List<TeacherReportRow>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TeacherReportRow>>() {}
        );
        return response.getBody();
    }

    public ReportSummary buildSummary() {
        List<TeacherReportRow> teachers = findAllTeachers();
        long working = teachers.stream()
                .filter(t -> "WORKING".equals(t.statusCode()))
                .count();
        long fired = teachers.stream()
                .filter(t -> "FIRED".equals(t.statusCode()))
                .count();
        return new ReportSummary(working, fired);
    }
}