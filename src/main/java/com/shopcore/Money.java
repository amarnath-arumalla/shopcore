package com.shopcore;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Money(BigDecimal amount) implements Comparable<Money> {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money cannot be negative: " + amount);
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Money cannot have more than 2 decimal places: " + amount);
        }
        amount = amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money of(String value) {
        return new Money(new BigDecimal(value));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        if (other.isGreaterThan(this)) {
            throw new IllegalArgumentException("Cannot subtract " + other + " from " + this);
        }
        return new Money(amount.subtract(other.amount));
    }

    public Money times(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public Money percent(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        }
        BigDecimal result = amount.multiply(BigDecimal.valueOf(percent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
        return new Money(result);
    }

    public boolean isGreaterThan(Money other) {
        return amount.compareTo(other.amount) > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return "$" + amount;
    }
}
