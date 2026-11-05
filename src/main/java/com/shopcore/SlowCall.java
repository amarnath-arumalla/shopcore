package com.shopcore;

final class SlowCall {

    private SlowCall() {
    }

    // Pretends to wait for a network call to another company's system.
    static void simulateNetworkDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting", e);
        }
    }
}
