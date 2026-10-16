package ru.ulstu.report_service.config;

import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .additionalInterceptors((request, body, execution) -> {
                    String traceId = MDC.get(TraceIdFilter.TRACE_MDC_KEY);
                    if (traceId != null) {
                        request.getHeaders().set(TraceIdFilter.TRACE_HEADER, traceId);
                    }

                    ServletRequestAttributes attrs =
                            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    if (attrs != null) {
                        String auth = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                        if (auth != null) {
                            request.getHeaders().set(HttpHeaders.AUTHORIZATION, auth);
                        }
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}