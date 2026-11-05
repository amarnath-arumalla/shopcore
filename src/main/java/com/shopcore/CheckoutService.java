package com.shopcore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class CheckoutService {

    static final String NO_ESTIMATE = "Delivery estimate unavailable, we will email you";

    private final OrderService orderService;
    private final FraudCheckService fraudCheck;
    private final ShippingService shipping;
    private final EmailService email;
    private final Executor executor;
    private final long shippingTimeoutMillis;

    public CheckoutService(OrderService orderService, FraudCheckService fraudCheck,
                           ShippingService shipping, EmailService email,
                           Executor executor, long shippingTimeoutMillis) {
        this.orderService = orderService;
        this.fraudCheck = fraudCheck;
        this.shipping = shipping;
        this.email = email;
        this.executor = executor;
        this.shippingTimeoutMillis = shippingTimeoutMillis;
    }

    // The old way: every step waits for the one before it.
    public CheckoutResult checkoutSequential(ShoppingCart cart, PaymentMethod paymentMethod) {
        Order order = orderService.placeOrder(cart);
        if (fraudCheck.isSuspicious(order)) {
            orderService.cancelOrder(order);
            throw new FraudSuspectedException(order.getOrderId());
        }
        String delivery = shipping.estimateDelivery(order);
        PaymentResult payment = orderService.payOrder(order, paymentMethod);
        email.sendConfirmation(order, delivery);
        return new CheckoutResult(order.getOrderId(), order.getTotal(), payment.transactionId(), delivery);
    }

    // The new way: independent steps run at the same time.
    public CompletableFuture<CheckoutResult> checkoutAsync(ShoppingCart cart, PaymentMethod paymentMethod) {
        // Step 1: reserve stock right now (fast, local, uses our locks).
        Order order = orderService.placeOrder(cart);

        // Step 2: start the fraud check and the shipping estimate AT THE SAME TIME.
        CompletableFuture<Boolean> fraudFuture =
                CompletableFuture.supplyAsync(() -> fraudCheck.isSuspicious(order), executor);

        CompletableFuture<String> deliveryFuture =
                CompletableFuture.supplyAsync(() -> shipping.estimateDelivery(order), executor)
                        .completeOnTimeout(NO_ESTIMATE, shippingTimeoutMillis, TimeUnit.MILLISECONDS);

        // Step 3: when BOTH are done, decide and take payment.
        CompletableFuture<CheckoutResult> checkout = fraudFuture.thenCombine(deliveryFuture,
                (suspicious, delivery) -> {
                    if (suspicious) {
                        throw new FraudSuspectedException(order.getOrderId());
                    }
                    PaymentResult payment = orderService.payOrder(order, paymentMethod);
                    return new CheckoutResult(order.getOrderId(), order.getTotal(),
                            payment.transactionId(), delivery);
                });

        // Step 4: clean up on failure, or send the email on success (without making the customer wait).
        return checkout.whenComplete((result, error) -> {
            if (error != null) {
                if (order.getStatus() == OrderStatus.PLACED) {
                    orderService.cancelOrder(order);   // give the stock back
                }
            } else {
                CompletableFuture.runAsync(() -> email.sendConfirmation(order, result.delivery()), executor);
            }
        });
    }
}
