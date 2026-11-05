package com.shopcore;

public class ShippingService {

    private final long delayMillis;

    public ShippingService(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public String estimateDelivery(Order order) {
        SlowCall.simulateNetworkDelay(delayMillis);
        return "Arrives in 2 days";
    }
}