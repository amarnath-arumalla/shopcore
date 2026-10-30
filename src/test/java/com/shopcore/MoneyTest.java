package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MoneyTest {

    @Test
    @DisplayName("10.5 and 10.50 are the same amount of money")
    void equalsIgnoresTrailingZeros() {
        Money a = Money.of("10.5");
        Money b = Money.of("10.50");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void plusReturnsNewMoneyAndLeavesOriginalUnchanged() {
        Money ten = Money.of("10.00");

        Money fifteen = ten.plus(Money.of("5.00"));

        assertEquals(Money.of("15.00"), fifteen);
        assertEquals(Money.of("10.00"), ten);
    }

    @Test
    void percentRoundsHalfEvenToTheCent() {
        // 10% of 55.49 is 5.549, which rounds to 5.55
        assertEquals(Money.of("5.55"), Money.of("55.49").percent(10));
    }

    @Test
    void minusCannotGoBelowZero() {
        Money five = Money.of("5.00");

        assertThrows(IllegalArgumentException.class, () -> five.minus(Money.of("5.01")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1.00", "-0.01", "10.005", "0.001"})
    void rejectsInvalidAmounts(String value) {
        assertThrows(IllegalArgumentException.class, () -> Money.of(value));
    }

    @Test
    void differentAmountsAreNotEqual() {
        assertNotEquals(Money.of("10.00"), Money.of("10.01"));
    }
}
