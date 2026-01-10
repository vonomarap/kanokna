package com.kanokna.cart.adapters.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for cart-service.
 */
@ConfigurationProperties(prefix = "cart")
public class CartProperties {
    private Duration anonymousTtl = Duration.ofDays(7);
    private Duration abandonedThreshold = Duration.ofHours(72);
    private Duration snapshotValidity = Duration.ofMinutes(15);

    public Duration getAnonymousTtl() {
        return anonymousTtl;
    }

    public void setAnonymousTtl(Duration anonymousTtl) {
        this.anonymousTtl = anonymousTtl;
    }

    public Duration getAbandonedThreshold() {
        return abandonedThreshold;
    }

    public void setAbandonedThreshold(Duration abandonedThreshold) {
        this.abandonedThreshold = abandonedThreshold;
    }

    public Duration getSnapshotValidity() {
        return snapshotValidity;
    }

    public void setSnapshotValidity(Duration snapshotValidity) {
        this.snapshotValidity = snapshotValidity;
    }
}
