package ru.ulstu.auth_service.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdWebFilter implements WebFilter {

    public static final String TRACE_HEADER = "X-Trace-Id";
    public static final String TRACE_MDC_KEY = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        String finalTraceId = traceId;

        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.headers(h -> h.set(TRACE_HEADER, finalTraceId)))
                .build();
        exchange.getResponse().getHeaders().set(TRACE_HEADER, finalTraceId);

        return chain.filter(mutated)
                .contextWrite(ctx -> ctx.put(TRACE_MDC_KEY, finalTraceId));
    }
}