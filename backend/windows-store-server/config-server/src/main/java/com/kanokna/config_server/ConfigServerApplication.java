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
 * Spring Cloud Config Server entry point.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    /**
     * Startup block marker for canonical logs.
     */
    private static final String STARTUP_BLOCK = "BA-CFG-SRV-STARTUP";

    /**
     * Startup log template in canonical format.
     */
    private static final String STARTUP_LOG_TEMPLATE =
            "[SVC=config-server][BLOCK={}][STATE=READY] "
                    + "eventType=CONFIG_SERVER_STARTED "
                    + "activeProfiles={} configSource={} "
                    + "port={} actuatorPort={}";

    /**
     * Class logger.
     */
    private static final Logger LOG =
            LoggerFactory.getLogger(ConfigServerApplication.class);

    /**
     * Spring environment used for profile and port lookup.
     */
    private final Environment environment;

    /**
     * Creates the application component.
     *
     * @param appEnvironment Spring runtime environment
     */
    public ConfigServerApplication(final Environment appEnvironment) {
        this.environment = appEnvironment;
    }

    /**
     * Starts the Config Server application.
     *
     * @param args startup arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

    /**
     * Logs startup information for operational visibility.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        final String[] activeProfiles = environment.getActiveProfiles();
        final String profilesDisplay = activeProfiles.length > 0
                ? String.join(", ", activeProfiles)
                : "default";

        final String configSource = determineConfigSource(activeProfiles);

        LOG.info(
                STARTUP_LOG_TEMPLATE,
                STARTUP_BLOCK,
                profilesDisplay,
                configSource,
                environment.getProperty("server.port", "8888"),
                environment.getProperty("management.server.port", "8889")
        );
    }

    /**
     * Determines the active configuration source.
     *
     * @param activeProfiles active Spring profiles
     * @return {@code git} for stage/prod/git profiles, otherwise {@code native}
     */
    private String determineConfigSource(final String[] activeProfiles) {
        for (final String profile : activeProfiles) {
            if ("prod".equalsIgnoreCase(profile)
                    || "stage".equalsIgnoreCase(profile)
                    || "git".equalsIgnoreCase(profile)) {
                return "git";
            }
        }
        return "native";
    }
}
