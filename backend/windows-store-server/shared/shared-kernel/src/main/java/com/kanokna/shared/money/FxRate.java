package com.kanokna.shared.money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** Immutable FX rate from -> to with timestamp. */
public final class FxRate {
    private final Currency from;
    private final Currency to;
    private final BigDecimal rate; // multiply base amount by rate to get target
    private final Instant asOf;

    private FxRate(Currency from, Currency to, BigDecimal rate, Instant asOf) {
        if (from == null || to == null) 
            throw new IllegalArgumentException("currencies required");
        if (from == to) 
            throw new IllegalArgumentException("from and to must differ");
        if (rate == null || rate.signum() <= 0) 
            throw new IllegalArgumentException("rate must be > 0");

        this.from = from;
        this.to = to;
        this.rate = rate;
        this.asOf = Objects.requireNonNullElseGet(asOf, Instant::now);
    }

    public static FxRate of(Currency from, Currency to, BigDecimal rate, Instant asOf) {
        return new FxRate(from, to, rate, asOf);
    }

    public Currency from() { 
        return from; 
    }
    public Currency to() { 
        return to; 
    }
    public BigDecimal rate() { 
        return rate; 
    }
    public Instant asOf() { 
        return asOf; 
    }
}

