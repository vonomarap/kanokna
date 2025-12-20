package com.kanokna.order_service.adapters.out.memory;

import com.kanokna.order_service.application.port.out.IdempotencyStore;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryIdempotencyStore implements IdempotencyStore {

    private final Set<String> keys = ConcurrentHashMap.newKeySet();

    @Override
    public boolean exists(String key) {
        return keys.contains(key);
    }

    @Override
    public void put(String key) {
        if (key != null && !key.isBlank()) {
            keys.add(key);
        }
    }
}
