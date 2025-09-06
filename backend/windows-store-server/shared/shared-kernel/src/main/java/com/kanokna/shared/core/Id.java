package shared.shared_kernel;

import java.util.Objects;
import java.util.UUID;

public final class Id {
    private final String value;

    private Id(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ID must be non-empty");
        }
        this.value = value;
    }

    public static Id of(String value) {
        return new Id(value);
    }

    public static Id random() {
        return new Id(UUID.randomUUID().toString());
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) 
            return true;
        if(!(o instanceof Id that)) 
            return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override 
    public String toString() {
        return value;
    }
}
