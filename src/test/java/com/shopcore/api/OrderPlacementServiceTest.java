package com.shopcore.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.shopcore.api.OrderRepository.CreatedOrder;

class OrderPlacementServiceTest {

    private OrderRepository orderRepository;
    private ProductCatalogRepository productRepository;
    private OrderPlacementService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        productRepository = mock(ProductCatalogRepository.class);
        service = new OrderPlacementService(orderRepository, productRepository);

        when(orderRepository.customerExists(1L)).thenReturn(true);
        when(orderRepository.insertOrder(1L)).thenReturn(
                new CreatedOrder(10L, "ORD-2001", "PLACED", BigDecimal.ZERO, OffsetDateTime.now()));
    }

    private static OrderLineRequest line(String sku, int qty) {
        return new OrderLineRequest(sku, qty);
    }

    @Test
    void reservesStockInSkuOrderAndComputesTotal() {
        when(productRepository.reserveStock("SKU-A", 1))
                .thenReturn(Optional.of(new StockReservation(1L, "Alpha", new BigDecimal("10.00"))));
        when(productRepository.reserveStock("SKU-B", 2))
                .thenReturn(Optional.of(new StockReservation(2L, "Beta", new BigDecimal("2.50"))));

        // client sends B before A
        OrderResponse result = service.placeOrder(
                new PlaceOrderRequest(1L, List.of(line("SKU-B", 2), line("SKU-A", 1))));

        InOrder order = inOrder(productRepository);
        order.verify(productRepository).reserveStock("SKU-A", 1);   // A first: sorted
        order.verify(productRepository).reserveStock("SKU-B", 2);

        assertThat(result.orderNumber()).isEqualTo("ORD-2001");
        assertThat(result.subtotal()).isEqualByComparingTo("15.00");   // 10.00 + 2 x 2.50
        assertThat(result.total()).isEqualByComparingTo("15.00");
    }

    @Test
    void duplicateSkuIsRejectedBeforeTouchingTheDatabase() {
        var request = new PlaceOrderRequest(1L, List.of(line("SKU-A", 1), line("SKU-A", 2)));

        assertThrows(InvalidOrderException.class, () -> service.placeOrder(request));
        verify(orderRepository, never()).insertOrder(anyLong());
    }

    @Test
    void unknownCustomerIsRejected() {
        when(orderRepository.customerExists(99L)).thenReturn(false);
        var request = new PlaceOrderRequest(99L, List.of(line("SKU-A", 1)));

        assertThrows(CustomerNotFoundException.class, () -> service.placeOrder(request));
        verify(orderRepository, never()).insertOrder(anyLong());
    }

    @Test
    void tooLittleStockReportsWhatIsAvailable() {
        when(productRepository.reserveStock("SKU-A", 50)).thenReturn(Optional.empty());
        when(productRepository.findBySku("SKU-A")).thenReturn(Optional.of(
                new ProductResponse("SKU-A", "Alpha", ProductCategory.ELECTRONICS,
                        new BigDecimal("10.00"), 5, 0)));

        InsufficientStockException ex = assertThrows(InsufficientStockException.class,
                () -> service.placeOrder(new PlaceOrderRequest(1L, List.of(line("SKU-A", 50)))));

        assertThat(ex.getAvailable()).isEqualTo(5);
    }
}