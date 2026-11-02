package com.shopcore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class OrderService {

    private final ProductCatalog catalog;
    private final AtomicInteger nextOrderNumber = new AtomicInteger(1001);

    // One lock per product SKU, created the first time that SKU is ordered.
    private final Map<String, ReentrantLock> skuLocks = new ConcurrentHashMap<>();

    public OrderService(ProductCatalog catalog) {
        if (catalog == null) {
            throw new IllegalArgumentException("Catalog is required");
        }
        this.catalog = catalog;
    }

    public Order placeOrder(ShoppingCart cart) {
        return placeOrder(cart, List.of());
    }

    public Order placeOrder(ShoppingCart cart, List<DiscountRule> discountRules) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Cannot place an order with an empty cart");
        }
        if (discountRules == null) {
            throw new IllegalArgumentException("Discount rules are required (use List.of() for none)");
        }

        // Lock every product in this cart, ALWAYS in alphabetical SKU order.
        List<ReentrantLock> locks = locksInSkuOrder(cart);
        for (ReentrantLock lock : locks) {
            lock.lock();
        }
        try {
            return placeOrderWhileLocked(cart, discountRules);
        } finally {
            for (int i = locks.size() - 1; i >= 0; i--) {
                locks.get(i).unlock();
            }
        }
    }

    private List<ReentrantLock> locksInSkuOrder(ShoppingCart cart) {
        TreeSet<String> sortedSkus = new TreeSet<>();
        for (CartItem item : cart.getItems()) {
            sortedSkus.add(item.getProduct().getSku());
        }
        List<ReentrantLock> locks = new ArrayList<>();
        for (String sku : sortedSkus) {
            locks.add(skuLocks.computeIfAbsent(sku, key -> new ReentrantLock()));
        }
        return locks;
    }

    private Order placeOrderWhileLocked(ShoppingCart cart, List<DiscountRule> discountRules) {
        // Step 1: check EVERY item first. Change nothing yet.
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            if (item.getQuantity() > product.getStock()) {
                throw new OutOfStockException(product.getSku(), product.getStock(), item.getQuantity());
            }
        }

        // Step 2: copy each line (price snapshot). Still nothing changed.
        List<OrderLine> lines = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            lines.add(new OrderLine(product.getSku(), product.getName(),
                    product.getPrice(), item.getQuantity()));
        }

        // Step 3: pick the single best discount. Discounts do not stack.
        DiscountRule bestRule = null;
        Money bestDiscount = Money.ZERO;
        for (DiscountRule rule : discountRules) {
            Money discount = rule.discountFor(lines);
            if (discount.isGreaterThan(bestDiscount)) {
                bestRule = rule;
                bestDiscount = discount;
            }
        }
        String description = (bestRule == null) ? "No discount" : bestRule.getDescription();

        // Step 4: build the order with a unique ID. If this throws, stock is still untouched.
        Order order = new Order("ORD-" + nextOrderNumber.getAndIncrement(), lines, bestDiscount, description);

        // Step 5: everything is valid, so now change state.
        for (CartItem item : cart.getItems()) {
            item.getProduct().removeStock(item.getQuantity());
        }
        cart.clear();
        return order;
    }

    public void cancelOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }

        // order.cancel() is synchronized, so only ONE caller can ever succeed.
        // Everyone else gets InvalidOrderStateException and never restocks.
        order.cancel();

        for (OrderLine line : order.getLines()) {
            Product product = catalog.getBySku(line.sku());
            product.addStock(line.quantity());
        }
    }

    public PaymentResult payOrder(Order order, PaymentMethod paymentMethod) {
        if (order == null || paymentMethod == null) {
            throw new IllegalArgumentException("Order and payment method are required");
        }

        // Lock THIS order only. Two different orders can still be paid at the same time.
        synchronized (order) {
            // Step 1: make sure the order can be paid BEFORE taking any money.
            if (order.getStatus() != OrderStatus.PLACED) {
                throw new InvalidOrderStateException(order.getOrderId(), order.getStatus(), "pay");
            }

            // Step 2: charge. We don't know or care which kind of payment this is.
            PaymentResult result = paymentMethod.charge(order.getTotal());

            // Step 3: only mark the order paid if the money actually came through.
            if (!result.success()) {
                throw new PaymentFailedException(order.getOrderId(), result.message());
            }
            order.pay();
            return result;
        }
    }
}