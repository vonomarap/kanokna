package com.kanokna.gateway.filter;

import java.net.URI;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.nanoTime();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethodValue();

        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = route != null ? route.getId() : "unknown";
        URI target = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);

        MDC.put("path", path);
        MDC.put("method", method);

        logger.info(
            "[SVC=gateway][UC=ROUTING][BLOCK=BA-GW-ROUTE-01][STATE=ROUTE_MATCHED] " +
            "eventType=REQUEST_ROUTED decision=FORWARD keyValues=path={},routeId={},target={}",
            path,
            routeId,
            Objects.toString(target, "unknown")
        );

        return chain.filter(exchange)
            .doFinally(signal -> {
                long latencyMs = (System.nanoTime() - start) / 1_000_000L;
                HttpStatus status = exchange.getResponse().getStatusCode();
                String statusValue = status != null ? String.valueOf(status.value()) : "unknown";

                MDC.put("status", statusValue);
                MDC.put("latencyMs", String.valueOf(latencyMs));

                logger.info(
                    "[SVC=gateway][UC=ROUTING][BLOCK=BA-GW-ROUTE-03][STATE=RESPONSE_RECEIVED] " +
                    "eventType=BACKEND_RESPONSE decision=COMPLETE keyValues=status={},latencyMs={}",
                    statusValue,
                    latencyMs
                );

                MDC.remove("path");
                MDC.remove("method");
                MDC.remove("status");
                MDC.remove("latencyMs");
            });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
