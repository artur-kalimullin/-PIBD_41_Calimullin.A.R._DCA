package ru.ulstu.gateway.web;

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

        result.put("systemCpuUsage", getMetricValue("system.cpu.usage"));
        result.put("processCpuUsage", getMetricValue("process.cpu.usage"));
        result.put("cpuCount", getMetricValue("system.cpu.count"));
        result.put("jvmMemoryUsed", getMetricValue("jvm.memory.used"));
        result.put("jvmMemoryMax", getMetricValue("jvm.memory.max"));
        result.put("jvmThreadsLive", getMetricValue("jvm.threads.live"));
        result.put("processUptime", getMetricValue("process.uptime"));

        // Специфично для шлюза
        result.put("gatewayRoutesCount",
                getMetricValue("spring.cloud.gateway.routes.count"));

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