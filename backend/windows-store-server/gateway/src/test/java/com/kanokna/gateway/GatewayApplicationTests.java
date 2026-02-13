package com.kanokna.gateway;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
    "spring.cloud.config.enabled=false"
})
@Import(StubJwtDecoderConfig.class)
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
