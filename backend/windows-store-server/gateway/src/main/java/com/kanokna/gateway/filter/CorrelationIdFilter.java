/* <FUNCTION_CONTRACT id="FC-gateway-filter-CorrelationIdFilter-filter"
     LAYER="filter"
     INTENT="Inject or propagate X-Correlation-ID header for distributed tracing"
     INPUT="ServerWebExchange (incoming request)"
     OUTPUT="Mono<Void> (modified exchange with correlation ID)"
     SIDE_EFFECTS="Adds correlation ID to MDC for logging, modifies request headers"
     LINKS="RequirementsAnalysis.xml#NFR-OBS-TRACING;Technology.xml#TECH-opentelemetry">
  <PRECONDITIONS>
    <Item>ServerWebExchange is not null</Item>
    <Item>GatewayFilterChain is not null</Item>
  </PRECONDITIONS>

  <POSTCONDITIONS>
    <Item>X-Correlation-ID header present on request to downstream service</Item>
    <Item>X-Correlation-ID header present on response to client</Item>
    <Item>Correlation ID available in MDC for log statements</Item>
  </POSTCONDITIONS>

  <INVARIANTS>
    <Item>Correlation ID is a non-blank string (max 128 chars)</Item>
    <Item>If incoming request has X-Correlation-ID, it is preserved (not replaced)</Item>
    <Item>Filter executes on every request (ordered before routing)</Item>
  </INVARIANTS>

  <ERROR_HANDLING>
    <Item type="TECHNICAL" code="N/A">Filter chain exceptions propagate; correlation ID still logged</Item>
  </ERROR_HANDLING>

  <BLOCK_ANCHORS>
    <Item id="BA-GW-CORR-01">Extract or generate correlation ID</Item>
    <Item id="BA-GW-CORR-02">Set MDC context</Item>
    <Item id="BA-GW-CORR-03">Mutate request with correlation ID header</Item>
    <Item id="BA-GW-CORR-04">Add correlation ID to response headers</Item>
  </BLOCK_ANCHORS>

  <LOGGING>
    <Item>[SVC=gateway][UC=TRACING][BLOCK=BA-GW-CORR-01][STATE=EXTRACT_OR_GENERATE] eventType=CORRELATION_ID decision=EXTRACTED|GENERATED keyValues=correlationId</Item>
  </LOGGING>

  <TESTS>
    <Case id="TC-CORR-001">Request without X-Correlation-ID gets new UUID generated</Case>
    <Case id="TC-CORR-002">Request with X-Correlation-ID preserves existing value</Case>
    <Case id="TC-CORR-003">Response includes X-Correlation-ID header</Case>
    <Case id="TC-CORR-004">Logs include correlation ID in MDC</Case>
  </TESTS>
</FUNCTION_CONTRACT> */
package com.kanokna.gateway.filter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(CorrelationIdFilter.class);
    static final String HEADER_NAME = "X-Correlation-ID";
    private static final int MAX_CORRELATION_ID_LENGTH = 128;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // <BLOCK_ANCHOR id="BA-GW-CORR-01">Extract or generate correlation ID</BLOCK_ANCHOR>
        String incoming = exchange.getRequest().getHeaders().getFirst(HEADER_NAME);
        boolean extracted = isValidCorrelationId(incoming);
        String correlationId = extracted ? incoming : UUID.randomUUID().toString();

        logger.info(
                "[SVC=gateway][UC=TRACING][BLOCK=BA-GW-CORR-01][STATE=EXTRACT_OR_GENERATE] "
                + "eventType=CORRELATION_ID decision={} keyValues=correlationId={}",
                extracted ? "EXTRACTED" : "GENERATED",
                correlationId
        );

        // <BLOCK_ANCHOR id="BA-GW-CORR-03">Mutate request with correlation ID header</BLOCK_ANCHOR>
        ServerHttpRequest request = exchange.getRequest().mutate().header(HEADER_NAME, correlationId).build();

        // <BLOCK_ANCHOR id="BA-GW-CORR-04">Add correlation ID to response headers</BLOCK_ANCHOR>
        exchange.getResponse().getHeaders().set(HEADER_NAME, correlationId);

        // <BLOCK_ANCHOR id="BA-GW-CORR-02">Set MDC context</BLOCK_ANCHOR>
        // Set MDC before chain invocation so downstream filters/handlers see it.
        // MDC is ThreadLocal-based and doesn't propagate across Netty event loop threads,
        // so we also propagate via Reactor Context for reactive subscribers.
        // doFinally ensures cleanup regardless of success/error/cancel.
        MDC.put("correlationId", correlationId);
        return chain.filter(exchange.mutate().request(request).build())
                .doFinally(signal -> MDC.remove("correlationId"))
                .contextWrite(Context.of("correlationId", correlationId));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Accept any non-blank string up to 128 characters as a valid correlation
     * ID. This allows UUID v1/v4/v7, OpenTelemetry trace IDs, and any custom
     * format from external systems (load balancers, clients).
     */
    private boolean isValidCorrelationId(String value) {
        return value != null && !value.isBlank() && value.length() <= MAX_CORRELATION_ID_LENGTH;
    }
}
