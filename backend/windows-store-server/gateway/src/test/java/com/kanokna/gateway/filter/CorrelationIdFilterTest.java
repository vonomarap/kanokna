package com.kanokna.gateway.filter;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void generatesCorrelationIdWhenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders/test").build()
        );
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String correlationId = chain.exchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER_NAME);
        assertThat(correlationId).isNotBlank();
        assertThat(chain.exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER_NAME))
                .isEqualTo(correlationId);
        assertThat(chain.mdcValue).isEqualTo(correlationId);
    }

    @Test
    void preservesCorrelationIdWhenPresent() {
        String existing = UUID.randomUUID().toString();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders/test").header(CorrelationIdFilter.HEADER_NAME, existing).build()
        );
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String correlationId = chain.exchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER_NAME);
        assertThat(correlationId).isEqualTo(existing);
        assertThat(chain.exchange.getResponse().getHeaders().getFirst(CorrelationIdFilter.HEADER_NAME))
                .isEqualTo(existing);
        assertThat(chain.mdcValue).isEqualTo(existing);
    }

    @Test
    void preservesNonUuidCorrelationId() {
        // OpenTelemetry trace IDs, custom formats from load balancers, etc.
        String traceId = "4bf92f3577b34da6a3ce929d0e0e4736";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test").header(CorrelationIdFilter.HEADER_NAME, traceId).build()
        );
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String correlationId = chain.exchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER_NAME);
        assertThat(correlationId).isEqualTo(traceId);
    }

    @Test
    void generatesNewIdForBlankHeader() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/test").header(CorrelationIdFilter.HEADER_NAME, "   ").build()
        );
        CapturingChain chain = new CapturingChain();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        String correlationId = chain.exchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.HEADER_NAME);
        assertThat(correlationId).isNotBlank();
        assertThat(correlationId).isNotEqualTo("   ");
    }

    private static final class CapturingChain implements GatewayFilterChain {

        private ServerWebExchange exchange;
        private String mdcValue;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.exchange = exchange;
            this.mdcValue = MDC.get("correlationId");
            MockServerHttpResponse response = (MockServerHttpResponse) exchange.getResponse();
            response.setComplete();
            return Mono.empty();
        }
    }
}
