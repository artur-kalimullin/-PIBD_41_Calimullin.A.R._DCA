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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final RestTemplate restTemplate;
    private final ReportQueueProducer queueProducer;
    private final String teacherServiceUrl;

    public ReportService(RestTemplate restTemplate,
                         ReportQueueProducer queueProducer,
                         @Value("${teacher.service.url}") String teacherServiceUrl) {
        this.restTemplate = restTemplate;
        this.queueProducer = queueProducer;
        this.teacherServiceUrl = teacherServiceUrl;
    }

    public List<TeacherReportRow> findAllTeachers() {
        String url = teacherServiceUrl + "/api/teachers/all";
        log.info("REPORT -> teacher-service: GET {}", url);
        ResponseEntity<List<TeacherReportRow>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<TeacherReportRow>>() {});
        List<TeacherReportRow> body = response.getBody();
        log.info("REPORT <- teacher-service: status={} rows={}",
                response.getStatusCode().value(), body == null ? 0 : body.size());
        return body;
    }

    public ReportSummary buildSummary() {
        List<TeacherReportRow> teachers = findAllTeachers();
        long working = teachers == null ? 0 : teachers.stream()
                .filter(t -> "WORKING".equals(t.statusCode())).count();
        long fired = teachers == null ? 0 : teachers.stream()
                .filter(t -> "FIRED".equals(t.statusCode())).count();
        log.info("REPORT: summary working={} fired={}", working, fired);
        return new ReportSummary(working, fired);
    }

    private byte[] buildCsv() {
        List<TeacherReportRow> teachers = findAllTeachers();
        StringBuilder sb = new StringBuilder();
        sb.append("id,fullName,position,statusCode,statusName,fireReason,subject,hiredAt,firedAt,createdAt\n");
        if (teachers != null) {
            for (TeacherReportRow t : teachers) {
                sb.append(csv(t.id())).append(',')
                  .append(csv(t.fullName())).append(',')
                  .append(csv(t.position())).append(',')
                  .append(csv(t.statusCode())).append(',')
                  .append(csv(t.statusName())).append(',')
                  .append(csv(t.fireReason())).append(',')
                  .append(csv(t.subject())).append(',')
                  .append(csv(t.hiredAt())).append(',')
                  .append(csv(t.firedAt())).append(',')
                  .append(csv(t.createdAt())).append('\n');
            }
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public Map<String, Object> saveCsvToStorage() {
        byte[] csv = buildCsv();
        String filename = "teachers-report-"
                + Instant.now().toString().replace(":", "-") + ".csv";

        CsvReportMessage message = new CsvReportMessage(
                filename,
                Base64.getEncoder().encodeToString(csv));

        queueProducer.send(message);
        log.info("CSV report queued: name={} bytes={}", filename, csv.length);

        return Map.of(
                "status", "QUEUED",
                "filename", filename,
                "size", csv.length,
                "queue", "report.csv.queue"
        );
    }

    private String csv(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}