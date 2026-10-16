package ru.ulstu.teacher_service.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "reportTaskExecutor")
    public Executor reportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("report-async-");
        executor.setTaskDecorator(mdcTaskDecorator());
        executor.initialize();
        return executor;
    }

    private TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            return () -> {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                RequestAttributes prevAttrs = RequestContextHolder.getRequestAttributes();
                if (contextMap != null) MDC.setContextMap(contextMap);
                if (attrs != null) RequestContextHolder.setRequestAttributes(attrs);
                try {
                    runnable.run();
                } finally {
                    if (previous != null) MDC.setContextMap(previous);
                    else MDC.clear();
                    if (prevAttrs != null) RequestContextHolder.setRequestAttributes(prevAttrs);
                    else RequestContextHolder.resetRequestAttributes();
                }
            };
        };
    }
}