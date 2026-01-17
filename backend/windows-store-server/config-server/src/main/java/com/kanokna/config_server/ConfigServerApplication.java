package com.kanokna.config_server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * Spring Cloud Config Server for centralized configuration management.
 *
 * <pre>
 * MODULE_CONTRACT: MC-config-server-infrastructure
 * ────────────────────────────────────────────────────────────────────────────
 * ID:          MC-config-server-infrastructure
 * Service:     config-server
 * Type:        Infrastructure
 * Layer:       N/A (Infrastructure Service)
 *
 * Description:
 *   Centralized configuration server providing externalized configuration
 *   to all microservices in the Kanokna Windows & Doors platform.
 *
 * Responsibilities:
 *   - Serve configuration from native filesystem (dev) or Git (stage/prod)
 *   - Support encryption/decryption of sensitive property values
 *   - Enable runtime configuration refresh for clients
 *   - Provide health endpoints for K8s readiness/liveness probes
 *   - Apply profile-based configuration (dev, stage, prod)
 *
 * Configuration Sources:
 *   - Native Profile: file:${CONFIG_REPO_PATH}/application.yml (and {application}.yml)
 *   - Git Profile: ${CONFIG_GIT_URI} (branch ${CONFIG_GIT_BRANCH})
 *
 * Security:
 *   - Basic authentication for config endpoints
 *   - Symmetric encryption for sensitive values (ENCRYPT_KEY)
 *   - Actuator endpoints secured by role
 *
 * Links:
 *   - DevelopmentPlan.xml#DP-SVC-config-server
 *   - Technology.xml#TECH-spring-cloud
 *   - RequirementsAnalysis.xml#NFR-SEC-AUTHENTICATION
 *
 * Port: 8888 (HTTP), 8889 (Actuator)
 * ────────────────────────────────────────────────────────────────────────────
 * </pre>
 *
 * @author GRACE-CODER
 * @since 1.0.0
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ConfigServerApplication.class);

    private final Environment environment;

    public ConfigServerApplication(Environment environment) {
        this.environment = environment;
    }

    static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

    /**
     * Logs startup information including active profiles and configuration source.
     * Provides belief-state logging for operational visibility.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String[] activeProfiles = environment.getActiveProfiles();
        String profilesDisplay = activeProfiles.length > 0
                ? String.join(", ", activeProfiles)
                : "default";

        String configSource = determineConfigSource(activeProfiles);

        // BA-CFG-SRV-STARTUP: Belief-state log for config server startup
        log.info("[SVC=config-server][BLOCK=BA-CFG-SRV-STARTUP][STATE=READY] " +
                        "eventType=CONFIG_SERVER_STARTED " +
                        "activeProfiles={} configSource={} port={} actuatorPort={}",
                profilesDisplay,
                configSource,
                environment.getProperty("server.port", "8888"),
                environment.getProperty("management.server.port", "8889"));
    }

    private String determineConfigSource(String[] activeProfiles) {
        for (String profile : activeProfiles) {
            if ("prod".equalsIgnoreCase(profile)
                    || "stage".equalsIgnoreCase(profile)
                    || "git".equalsIgnoreCase(profile)) {
                return "git";
            }
        }
        return "native";
    }
}
