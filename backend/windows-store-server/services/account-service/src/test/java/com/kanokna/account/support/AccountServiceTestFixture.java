package com.kanokna.account.support;

import com.kanokna.account.application.port.out.*;
import com.kanokna.account.domain.model.SavedAddress;
import com.kanokna.account.domain.model.SavedConfiguration;
import com.kanokna.account.domain.model.UserProfile;
import com.kanokna.shared.core.Id;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.*;

public final class AccountServiceTestFixture {
    private AccountServiceTestFixture() {
    }

    public static class InMemoryUserProfileRepository implements UserProfileRepository {
        private final Map<String, UserProfile> store = new HashMap<>();
        private boolean failOnSave;

        public void setFailOnSave(boolean failOnSave) {
            this.failOnSave = failOnSave;
        }

        @Override
        public Optional<UserProfile> findById(Id userId) {
            return Optional.ofNullable(store.get(userId.value()));
        }

        @Override
        public UserProfile save(UserProfile profile) {
            if (failOnSave) {
                throw new OptimisticLockingFailureException("Optimistic lock");
            }
            store.put(profile.userId().value(), profile);
            return profile;
        }

        public void clear() {
            store.clear();
        }
    }

    public static class InMemorySavedAddressRepository implements SavedAddressRepository {
        private final Map<String, List<SavedAddress>> store = new HashMap<>();

        @Override
        public List<SavedAddress> findByUserId(Id userId) {
            return new ArrayList<>(store.getOrDefault(userId.value(), List.of()));
        }

        @Override
        public Optional<SavedAddress> findByUserIdAndId(Id userId, Id addressId) {
            return store.getOrDefault(userId.value(), List.of()).stream()
                .filter(address -> address.addressId().equals(addressId))
                .findFirst();
        }

        @Override
        public List<SavedAddress> saveAll(Id userId, List<SavedAddress> addresses) {
            store.put(userId.value(), new ArrayList<>(addresses));
            return new ArrayList<>(addresses);
        }

        @Override
        public void deleteByUserIdAndId(Id userId, Id addressId) {
            List<SavedAddress> current = new ArrayList<>(store.getOrDefault(userId.value(), List.of()));
            current.removeIf(address -> address.addressId().equals(addressId));
            store.put(userId.value(), current);
        }
    }

    public static class InMemorySavedConfigurationRepository implements SavedConfigurationRepository {
        private final Map<String, List<SavedConfiguration>> store = new HashMap<>();

        @Override
        public SavedConfiguration save(SavedConfiguration configuration) {
            List<SavedConfiguration> current = new ArrayList<>(store.getOrDefault(configuration.userId().value(), List.of()));
            current.add(configuration);
            store.put(configuration.userId().value(), current);
            return configuration;
        }

        @Override
        public List<SavedConfiguration> findByUserId(Id userId) {
            return new ArrayList<>(store.getOrDefault(userId.value(), List.of()));
        }

        @Override
        public Optional<SavedConfiguration> findByUserIdAndId(Id userId, Id configurationId) {
            return store.getOrDefault(userId.value(), List.of()).stream()
                .filter(config -> config.configurationId().equals(configurationId))
                .findFirst();
        }

        @Override
        public void deleteByUserIdAndId(Id userId, Id configurationId) {
            List<SavedConfiguration> current = new ArrayList<>(store.getOrDefault(userId.value(), List.of()));
            current.removeIf(config -> config.configurationId().equals(configurationId));
            store.put(userId.value(), current);
        }
    }

    public static class RecordingEventPublisher implements EventPublisher {
        private final List<Object> events = new ArrayList<>();

        @Override
        public <T> void publish(String topic, T event) {
            events.add(event);
        }

        public List<Object> events() {
            return events;
        }
    }

    public static class FixedCurrentUserProvider implements CurrentUserProvider {
        private CurrentUser currentUser;

        public void setCurrentUser(CurrentUser currentUser) {
            this.currentUser = currentUser;
        }

        @Override
        public CurrentUser currentUser() {
            return currentUser;
        }
    }
}
