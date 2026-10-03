package ru.ulstu.teacher_service.config;

import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .additionalInterceptors((request, body, execution) -> {
                    String traceId = MDC.get(TraceIdFilter.TRACE_MDC_KEY);
                    if (traceId != null) {
                        request.getHeaders().set(TraceIdFilter.TRACE_HEADER, traceId);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}