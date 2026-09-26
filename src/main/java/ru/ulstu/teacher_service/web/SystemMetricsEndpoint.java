package ru.ulstu.teacher_service.web;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Endpoint(id = "custom")
public class SystemMetricsEndpoint {

    private final MeterRegistry meterRegistry;

    public SystemMetricsEndpoint(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @ReadOperation
    public Map<String, Object> metrics() {
        Map<String, Object> result = new HashMap<>();

        // CPU всей системы (0.0 - 1.0)
        result.put("systemCpuUsage", getMetricValue("system.cpu.usage"));
        // CPU, которое использует именно это приложение
        result.put("processCpuUsage", getMetricValue("process.cpu.usage"));
        // Количество доступных процессоров
        result.put("cpuCount", getMetricValue("system.cpu.count"));
        // Использованная память JVM (в байтах)
        result.put("jvmMemoryUsed", getMetricValue("jvm.memory.used"));
        // Максимальная память JVM (в байтах)
        result.put("jvmMemoryMax", getMetricValue("jvm.memory.max"));
        // Живые потоки
        result.put("jvmThreadsLive", getMetricValue("jvm.threads.live"));
        // Время работы приложения (в секундах)
        result.put("processUptime", getMetricValue("process.uptime"));

        return result;
    }

    private Double getMetricValue(String metricName) {
        var meter = meterRegistry.find(metricName).meter();
        if (meter == null) {
            return null;
        }
        var measurements = meter.measure().iterator();
        if (!measurements.hasNext()) {
            return null;
        }
        return measurements.next().getValue();
    }
}