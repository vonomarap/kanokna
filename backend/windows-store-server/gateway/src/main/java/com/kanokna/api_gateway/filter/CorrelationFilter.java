package com.kanokna.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CorrelationFilter.class);
    public static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = StringUtils.hasText(request.getHeaders().getFirst(CORRELATION_ID))
            ? request.getHeaders().getFirst(CORRELATION_ID)
            : UUID.randomUUID().toString();

        ServerHttpRequest mutatedRequest = request.mutate()
            .header(CORRELATION_ID, correlationId)
            .build();
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().set(CORRELATION_ID, correlationId);
        logger.info("[GATEWAY] {} {} corrId={}", request.getMethod(), request.getURI().getPath(), correlationId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
