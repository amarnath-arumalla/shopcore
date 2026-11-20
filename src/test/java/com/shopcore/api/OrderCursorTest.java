package com.shopcore.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

class OrderCursorTest {

    @Test
    void encodeThenDecodeGivesTheSamePosition() {
        var cursor = new OrderCursor(OffsetDateTime.parse("2026-11-19T17:40:12.345678Z"), 57L);

        OrderCursor back = OrderCursor.decode(cursor.encode());

        assertThat(back).isEqualTo(cursor);
    }

    @Test
    void encodedCursorIsUrlSafe() {
        var cursor = new OrderCursor(OffsetDateTime.parse("2026-11-19T17:40:12.345678Z"), 57L);
        assertThat(cursor.encode()).doesNotContain("+", "/", "=");
    }

    @Test
    void garbageCursorIsRejected() {
        assertThrows(InvalidCursorException.class, () -> OrderCursor.decode("not-a-cursor!!"));
        assertThrows(InvalidCursorException.class, () -> OrderCursor.decode("aGVsbG8"));   // "hello", no '|'
    }
}
