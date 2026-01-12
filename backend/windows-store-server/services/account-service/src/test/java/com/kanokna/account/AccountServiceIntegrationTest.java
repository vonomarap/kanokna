package com.kanokna.account;

import com.kanokna.account.application.dto.*;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.application.port.out.CurrentUserProvider;
import com.kanokna.account.application.service.AccountApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(
    value = "com.kanokna.account.support.DockerAvailability#isDockerAvailable",
    disabledReason = "Docker is not available, skipping Testcontainers integration tests"
)
@SpringBootTest
@Import(AccountServiceIntegrationTest.TestAuthConfig.class)
@Testcontainers
class AccountServiceIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("accounts")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.cloud.config.enabled", () -> "false");
    }

    @Autowired
    private AccountApplicationService service;

    @Autowired
    private TestCurrentUserProvider currentUserProvider;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(
            userId.toString(),
            "user@example.com",
            "Jane",
            "Doe",
            null,
            Set.of("CUSTOMER")
        ));
    }

    @Test
    @DisplayName("Integration: getProfile auto-creates profile")
    void getProfileAutoCreates() {
        UserProfileDto profile = service.getProfile(new GetProfileQuery(userId));
        assertEquals(userId.toString(), profile.userId());
    }

    @Test
    @DisplayName("Integration: updateProfile persists changes")
    void updateProfilePersistsChanges() {
        service.getProfile(new GetProfileQuery(userId));
        UserProfileDto updated = service.updateProfile(new UpdateProfileCommand(
            userId,
            "New",
            "Name",
            null,
            "ru",
            "RUB"
        ));

        assertEquals("New", updated.firstName());
        assertEquals("Name", updated.lastName());
    }

    @Test
    @DisplayName("Integration: save configuration flow persists and lists")
    void saveConfigurationFlow() {
        UUID productTemplateId = UUID.randomUUID();
        String snapshot = "{"
            + "\"productTemplateId\":\"" + productTemplateId + "\","
            + "\"dimensions\":{\"width\":100,\"height\":120,\"unit\":\"mm\"}"
            + "}";
        SavedConfigurationDto saved = service.saveConfiguration(new SaveConfigurationCommand(
            userId,
            "Config A",
            productTemplateId,
            snapshot,
            null
        ));

        List<SavedConfigurationDto> configs = service.listConfigurations(new ListConfigurationsQuery(userId));
        assertEquals(1, configs.size());
        assertEquals(saved.configurationId(), configs.get(0).configurationId());
    }

    @Test
    @DisplayName("Integration: add address flow persists and lists")
    void addAddressFlow() {
        service.getProfile(new GetProfileQuery(userId));
        SavedAddressDto saved = service.addAddress(new AddAddressCommand(
            userId,
            new AddressDto("RU", "Moscow", "123456", "Main Street", null),
            "Home",
            true
        ));

        List<SavedAddressDto> addresses = service.listAddresses(new ListAddressesQuery(userId));
        assertEquals(1, addresses.size());
        assertEquals(saved.addressId(), addresses.get(0).addressId());
    }

    @Test
    @DisplayName("Integration: delete configuration removes entry")
    void deleteConfigurationRemovesEntry() {
        UUID productTemplateId = UUID.randomUUID();
        String snapshot = "{"
            + "\"productTemplateId\":\"" + productTemplateId + "\","
            + "\"dimensions\":{\"width\":100,\"height\":120,\"unit\":\"mm\"}"
            + "}";
        SavedConfigurationDto saved = service.saveConfiguration(new SaveConfigurationCommand(
            userId,
            "Config B",
            productTemplateId,
            snapshot,
            null
        ));

        service.deleteConfiguration(new DeleteConfigurationCommand(userId, UUID.fromString(saved.configurationId())));

        assertTrue(service.listConfigurations(new ListConfigurationsQuery(userId)).isEmpty());
    }

    @TestConfiguration
    static class TestAuthConfig {
        @Bean
        @Primary
        public TestCurrentUserProvider currentUserProvider() {
            return new TestCurrentUserProvider();
        }
    }

    static class TestCurrentUserProvider implements CurrentUserProvider {
        private CurrentUser currentUser;

        void setCurrentUser(CurrentUser currentUser) {
            this.currentUser = currentUser;
        }

        @Override
        public CurrentUser currentUser() {
            return currentUser;
        }
    }
}
