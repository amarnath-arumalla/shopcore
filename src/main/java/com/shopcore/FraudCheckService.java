package com.shopcore;

public class FraudCheckService {

    private static final Money SUSPICIOUS_ABOVE = Money.of("1000.00");
    private final long delayMillis;

    public FraudCheckService(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public boolean isSuspicious(Order order) {
        SlowCall.simulateNetworkDelay(delayMillis);
        return order.getTotal().isGreaterThan(SUSPICIOUS_ABOVE);
    }
}
