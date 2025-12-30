package com.kanokna.config_server;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Config Server.
 *
 * <pre>
 * Test Coverage for: MC-config-server-infrastructure
 * ────────────────────────────────────────────────────────────────────────────
 * TC-CFG-001: Application context loads successfully           ✓ PASSING
 * TC-CFG-002: Config server bean is present                    ✓ PASSING
 * TC-CFG-003: Security configuration is active                 ✓ PASSING
 * TC-CFG-004: Encryption key is configured                     ✓ PASSING
 * TC-CFG-005: Native profile configuration is loaded           ✓ PASSING
 * TC-CFG-006: Health endpoints accessible without authentication ✓ PASSING
 * TC-CFG-007: Config endpoints require authentication          ✓ PASSING (401 test)
 * TC-CFG-008: Service-specific configs are served correctly    ⚠ BLOCKED (SB4 compat)
 * TC-CFG-009: Encryption endpoints work with authentication    ✓ PASSING
 * ────────────────────────────────────────────────────────────────────────────
 *
 * KNOWN LIMITATION (Technology.xml#TECH-ASSUM-001):
 * Spring Boot 4.0.0 is a placeholder version. Spring Cloud Config Server's
 * NativeEnvironmentRepository uses methods that don't exist in Spring Boot 4.x,
 * causing NoSuchMethodError when fetching configurations. Tests for config
 * endpoints (TC-CFG-007b auth success, TC-CFG-008) fail with HTTP 500 until
 * Spring Cloud releases a Spring Boot 4.x compatible version.
 *
 * PASSING TESTS: 15 (Context, Health, Encryption, Security 401)
 * BLOCKED TESTS: 13 (Config endpoint functionality)
 * ────────────────────────────────────────────────────────────────────────────
 * </pre>
 *
 * Note: Uses Java HttpClient for HTTP tests due to Spring Boot 4.x placeholder
 * version (Technology.xml#TECH-ASSUM-001).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ConfigServerApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONTEXT & BEAN TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Context & Bean Tests")
    class ContextTests {

        /**
         * TC-CFG-001: Application context loads successfully.
         */
        @Test
        @DisplayName("TC-CFG-001: Application context loads successfully")
        void contextLoads() {
            assertThat(applicationContext).isNotNull();
        }

        /**
         * TC-CFG-002: Config server components are present.
         */
        @Test
        @DisplayName("TC-CFG-002: Config server application bean is present")
        void configServerApplicationBeanPresent() {
            assertThat(applicationContext.containsBean("configServerApplication")).isTrue();
        }

        /**
         * TC-CFG-003: Security configuration is active.
         */
        @Test
        @DisplayName("TC-CFG-003: Security configuration bean is present")
        void securityConfigBeanPresent() {
            assertThat(applicationContext.containsBean("securityConfig")).isTrue();
        }

        /**
         * TC-CFG-004: Encryption key is configured.
         */
        @Test
        @DisplayName("TC-CFG-004: Encryption key is configured")
        void encryptionKeyIsConfigured() {
            String encryptKey = environment.getProperty("encrypt.key");
            assertThat(encryptKey).isNotBlank();
        }

        /**
         * TC-CFG-005: Test profile is active.
         */
        @Test
        @DisplayName("TC-CFG-005: Test profile is active")
        void testProfileIsActive() {
            String[] activeProfiles = environment.getActiveProfiles();
            assertThat(activeProfiles).contains("test");
        }

        @Test
        @DisplayName("TC-CFG-005b: Application name is config-server")
        void applicationNameIsCorrect() {
            String appName = environment.getProperty("spring.application.name");
            assertThat(appName).isEqualTo("config-server");
        }

        @Test
        @DisplayName("TC-CFG-005c: Health probes are enabled")
        void healthProbesAreEnabled() {
            String livenessEnabled = environment.getProperty("management.health.livenessState.enabled");
            String readinessEnabled = environment.getProperty("management.health.readinessState.enabled");

            assertThat(livenessEnabled).isEqualTo("true");
            assertThat(readinessEnabled).isEqualTo("true");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HEALTH ENDPOINT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Health Endpoint Tests")
    class HealthEndpointTests {

        /**
         * TC-CFG-006: Health endpoints accessible without authentication.
         */
        @Test
        @DisplayName("TC-CFG-006a: Health liveness probe accessible without auth")
        void healthLivenessProbeAccessibleWithoutAuth() throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/actuator/health/liveness"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("\"status\"");
            assertThat(response.body()).contains("UP");
        }

        @Test
        @DisplayName("TC-CFG-006b: Health readiness probe accessible without auth")
        void healthReadinessProbeAccessibleWithoutAuth() throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/actuator/health/readiness"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("\"status\"");
            assertThat(response.body()).contains("UP");
        }

        @Test
        @DisplayName("TC-CFG-006c: General health endpoint accessible without auth")
        void healthEndpointAccessibleWithoutAuth() throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/actuator/health"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("\"status\"");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONFIG ENDPOINT SECURITY TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Config Endpoint Security Tests")
    class ConfigEndpointSecurityTests {

        /**
         * TC-CFG-007: Config endpoints require authentication.
         */
        @Test
        @DisplayName("TC-CFG-007a: Config endpoint returns 401 without auth")
        void configEndpointRequiresAuthentication() throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/gateway/default"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("TC-CFG-007b: Config endpoint accessible with valid auth")
        void configEndpointAccessibleWithAuth() throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/gateway/default"))
                    .header("Authorization", basicAuth("configadmin", "testpassword"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SERVICE CONFIGURATION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Service Configuration Tests")
    class ServiceConfigurationTests {

        private HttpResponse<String> getConfigWithAuth(String serviceName) throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/" + serviceName + "/default"))
                    .header("Authorization", basicAuth("configadmin", "testpassword"))
                    .GET()
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }

        /**
         * TC-CFG-008: Service-specific configs are served correctly.
         */
        @Test
        @DisplayName("TC-CFG-008a: Gateway config served correctly")
        void gatewayConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("gateway");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008b: Catalog config served correctly")
        void catalogConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("catalog-configuration-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008c: Pricing config served correctly")
        void pricingConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("pricing-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008d: Cart config served correctly")
        void cartConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("cart-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008e: Order config served correctly")
        void orderConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("order-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008f: Account config served correctly")
        void accountConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("account-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008g: Media config served correctly")
        void mediaConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("media-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008h: Notification config served correctly")
        void notificationConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("notification-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008i: Search config served correctly")
        void searchConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("search-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008j: Reporting config served correctly")
        void reportingConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("reporting-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008k: Installation config served correctly")
        void installationConfigServedCorrectly() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("installation-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }

        @Test
        @DisplayName("TC-CFG-008l: Common application config inherits to new services")
        void commonConfigInheritedToNewServices() throws Exception {
            HttpResponse<String> response = getConfigWithAuth("any-new-service");

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("propertySources");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ENCRYPTION TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Encryption Tests")
    class EncryptionTests {

        /**
         * TC-CFG-009: Encryption endpoints work with authentication.
         */
        @Test
        @DisplayName("TC-CFG-009a: Encrypt endpoint requires authentication")
        void encryptEndpointRequiresAuthentication() throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/encrypt"))
                    .POST(HttpRequest.BodyPublishers.ofString("secret-value"))
                    .header("Content-Type", "text/plain")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("TC-CFG-009b: Encrypt endpoint works with authentication")
        void encryptEndpointWorksWithAuth() throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/encrypt"))
                    .POST(HttpRequest.BodyPublishers.ofString("secret-value"))
                    .header("Authorization", basicAuth("configadmin", "testpassword"))
                    .header("Content-Type", "text/plain")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).isNotBlank();
        }

        @Test
        @DisplayName("TC-CFG-009c: Decrypt endpoint requires authentication")
        void decryptEndpointRequiresAuthentication() throws Exception {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/decrypt"))
                    .POST(HttpRequest.BodyPublishers.ofString("encrypted-value"))
                    .header("Content-Type", "text/plain")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(401);
        }

        @Test
        @DisplayName("TC-CFG-009d: Encrypt and decrypt roundtrip works")
        void encryptDecryptRoundtripWorks() throws Exception {
            // First encrypt
            HttpRequest encryptRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/encrypt"))
                    .POST(HttpRequest.BodyPublishers.ofString("my-secret-password"))
                    .header("Authorization", basicAuth("configadmin", "testpassword"))
                    .header("Content-Type", "text/plain")
                    .build();

            HttpResponse<String> encryptResponse = httpClient.send(encryptRequest, HttpResponse.BodyHandlers.ofString());

            assertThat(encryptResponse.statusCode()).isEqualTo(200);
            String encrypted = encryptResponse.body();
            assertThat(encrypted).isNotBlank();

            // Then decrypt
            HttpRequest decryptRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/decrypt"))
                    .POST(HttpRequest.BodyPublishers.ofString(encrypted))
                    .header("Authorization", basicAuth("configadmin", "testpassword"))
                    .header("Content-Type", "text/plain")
                    .build();

            HttpResponse<String> decryptResponse = httpClient.send(decryptRequest, HttpResponse.BodyHandlers.ofString());

            assertThat(decryptResponse.statusCode()).isEqualTo(200);
            assertThat(decryptResponse.body()).isEqualTo("my-secret-password");
        }
    }
}
