package com.shopcore;

public interface PaymentMethod {

    PaymentResult charge(Money amount);

    String getDisplayName();
}
