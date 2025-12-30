package com.kanokna.gateway.route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {
    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping("/fallback")
    public Mono<ResponseEntity<Void>> fallback(ServerHttpRequest request) {
        // <BLOCK_ANCHOR id="BA-GW-CB-03">Execute fallback on open circuit</BLOCK_ANCHOR>
        logger.warn(
            "[SVC=gateway][UC=ROUTING][BLOCK=BA-GW-CB-03][STATE=FALLBACK] " +
            "eventType=CIRCUIT_BREAKER decision=FALLBACK keyValues=path={}",
            request.getURI().getPath()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build());
    }
}
