package com.shopcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderServiceTest {

    private ProductCatalog catalog;
    private OrderService orderService;
    private Product mouse;
    private Product book;

    @BeforeEach
    void setUp() {
        catalog = new ProductCatalog();
        mouse = new Product("SKU-1001", "Wireless Mouse", Category.ELECTRONICS, Money.of("19.99"), 10);
        book = new Product("SKU-2001", "Clean Code", Category.BOOKS, Money.of("35.50"), 2);
        catalog.addProduct(mouse);
        catalog.addProduct(book);
        orderService = new OrderService(catalog);
    }

    private ShoppingCart cartWith(Product product, int quantity) {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(product, quantity);
        return cart;
    }

    @Test
    void placingOrderReducesStockAndEmptiesCart() {
        ShoppingCart cart = cartWith(mouse, 3);

        Order order = orderService.placeOrder(cart);

        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertEquals(7, mouse.getStock());
        assertTrue(cart.isEmpty());
    }

    @Test
    void orderIsAllOrNothingWhenOneItemRunsOut() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(mouse, 3);
        cart.addItem(book, 2);
        book.removeStock(1);   // someone else bought a book after we added it to our cart

        assertThrows(OutOfStockException.class, () -> orderService.placeOrder(cart));

        assertEquals(10, mouse.getStock());   // the mouse stock was NOT touched
        assertFalse(cart.isEmpty());          // the cart is kept so the customer can fix it
    }

    @Test
    void bestDiscountWinsAndDiscountsDoNotStack() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(mouse, 3);
        cart.addItem(book, 1);
        List<DiscountRule> promotions = List.of(
                new PercentageOffDiscount(10, Money.of("50.00")),
                new BuyXGetOneFreeDiscount("SKU-1001", 2));

        Order order = orderService.placeOrder(cart, promotions);

        assertEquals(Money.of("19.99"), order.getDiscount());
        assertEquals(Money.of("75.48"), order.getTotal());
    }

    @Test
    void cancellingPutsStockBack() {
        Order order = orderService.placeOrder(cartWith(mouse, 4));

        orderService.cancelOrder(order);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(10, mouse.getStock());
    }

    @Test
    void cancellingTwiceDoesNotRestockTwice() {
        Order order = orderService.placeOrder(cartWith(mouse, 4));
        orderService.cancelOrder(order);

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(order));
        assertEquals(10, mouse.getStock());
    }

    @Test
    void declinedPaymentLeavesOrderPlaced() {
        Order order = orderService.placeOrder(cartWith(book, 2));   // $71.00
        GiftCardPayment giftCard = new GiftCardPayment("GIFT-10", Money.of("10.00"));

        assertThrows(PaymentFailedException.class, () -> orderService.payOrder(order, giftCard));

        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertEquals(Money.of("10.00"), giftCard.getBalance());
    }

    @Test
    void payingTwiceIsBlockedBeforeAnyMoneyMoves() {
        Order order = orderService.placeOrder(cartWith(mouse, 1));
        GiftCardPayment giftCard = new GiftCardPayment("GIFT-50", Money.of("50.00"));
        orderService.payOrder(order, giftCard);

        assertThrows(InvalidOrderStateException.class, () -> orderService.payOrder(order, giftCard));

        assertEquals(Money.of("30.01"), giftCard.getBalance());
    }
}
