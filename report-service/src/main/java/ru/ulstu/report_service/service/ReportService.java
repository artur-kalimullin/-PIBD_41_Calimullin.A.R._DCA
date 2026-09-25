package ru.ulstu.report_service.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.ulstu.report_service.domain.ReportSummary;
import ru.ulstu.report_service.domain.TeacherReportRow;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final JdbcTemplate jdbc;

    public ReportService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TeacherReportRow> findAllTeachers() {
        String sql = """
                SELECT t.id,
                       t.full_name,
                       t.position,
                       s.code AS status_code,
                       s.name AS status_name,
                       t.fire_reason,
                       t.subject,
                       t.hired_at,
                       t.fired_at,
                       t.created_at
                FROM teacher t
                JOIN teacher_status s ON s.id = t.status_id
                ORDER BY t.id
                """;

        return jdbc.query(sql, (rs, rowNum) -> mapRow(rs));
    }

    public ReportSummary buildSummary() {
        String sql = """
                SELECT s.code AS code, COUNT(*) AS cnt
                FROM teacher t
                JOIN teacher_status s ON s.id = t.status_id
                GROUP BY s.code
                """;

        Map<String, Long> counts = new HashMap<>();
        List<Map<String, Object>> rows = jdbc.queryForList(sql);
        for (Map<String, Object> row : rows) {
            String code = (String) row.get("code");
            Number cnt = (Number) row.get("cnt");
            counts.put(code, cnt == null ? 0L : cnt.longValue());
        }

        return new ReportSummary(
                counts.getOrDefault("WORKING", 0L),
                counts.getOrDefault("FIRED", 0L)
        );
    }

    private TeacherReportRow mapRow(ResultSet rs) throws SQLException {
        return new TeacherReportRow(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("position"),
                rs.getString("status_code"),
                rs.getString("status_name"),
                rs.getString("fire_reason"),
                rs.getString("subject"),
                getInstant(rs, "hired_at"),
                getInstant(rs, "fired_at"),
                getInstant(rs, "created_at")
        );
    }

    private Instant getInstant(ResultSet rs, String column) throws SQLException {
        var ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toInstant();
    }
}