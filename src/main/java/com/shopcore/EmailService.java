package com.shopcore;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EmailService {

    private final long delayMillis;
    private final List<String> sent = new CopyOnWriteArrayList<>();

    public EmailService(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public void sendConfirmation(Order order, String delivery) {
        SlowCall.simulateNetworkDelay(delayMillis);
        sent.add(order.getOrderId() + ": " + delivery);
    }

    public List<String> getSent() {
        return List.copyOf(sent);
    }
}