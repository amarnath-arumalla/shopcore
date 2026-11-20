package com.shopcore.api;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;

/** Bookmark = position of the last order on the previous page. Sent to clients as an opaque string. */
public record OrderCursor(OffsetDateTime createdAt, long id) {

    public String encode() {
        String raw = createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static OrderCursor decode(String token) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            int bar = raw.lastIndexOf('|');
            return new OrderCursor(
                    OffsetDateTime.parse(raw.substring(0, bar)),
                    Long.parseLong(raw.substring(bar + 1)));
        } catch (RuntimeException e) {     // bad Base64, no '|', bad date, bad number...
            throw new InvalidCursorException();
        }
    }
}
