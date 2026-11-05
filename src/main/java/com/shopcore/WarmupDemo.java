package com.shopcore;

public class WarmupDemo {

    private static final int BATCH = 20_000;

    public static void main(String[] args) {
        System.out.println(">>> Starting. GiftCardPayment has not been used yet.");
        GiftCardPayment giftCard = new GiftCardPayment("GIFT-1", Money.of("5.00"));
        System.out.println(">>> Created a " + giftCard.getDisplayName());

        ProductCatalog catalog = new ProductCatalog();
        Product mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS,
                Money.of("19.99"), Integer.MAX_VALUE);
        catalog.addProduct(mouse);
        OrderService orderService = new OrderService(catalog);

        for (int batch = 1; batch <= 8; batch++) {
            long start = System.nanoTime();
            for (int i = 0; i < BATCH; i++) {
                ShoppingCart cart = new ShoppingCart();
                cart.addItem(mouse, 1);
                orderService.placeOrder(cart);
            }
            long nanosPerCheckout = (System.nanoTime() - start) / BATCH;
            System.out.println("Batch " + batch + ": " + nanosPerCheckout + " ns per checkout");
        }
    }
}