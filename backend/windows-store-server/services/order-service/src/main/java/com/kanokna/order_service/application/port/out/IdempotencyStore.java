package com.kanokna.order_service.application.port.out;

public interface IdempotencyStore {

    boolean exists(String key);

    void put(String key);
}
