package com.shopcore.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.shopcore.api.OrderQueryRepository.OrderRow;

class OrderQueryServiceTest {

    private OrderQueryRepository queryRepository;
    private OrderRepository orderRepository;
    private OrderQueryService service;

    private static OrderRow order(long id, String number, String createdAt) {
        return new OrderRow(id, number, 1L, "PLACED", BigDecimal.ZERO, OffsetDateTime.parse(createdAt));
    }

    private static OrderLineResponse line(String sku, String price, int qty) {
        BigDecimal p = new BigDecimal(price);
        return new OrderLineResponse(sku, sku + " name", p, qty, p.multiply(BigDecimal.valueOf(qty)));
    }

    @BeforeEach
    void setUp() {
        queryRepository = mock(OrderQueryRepository.class);
        orderRepository = mock(OrderRepository.class);
        service = new OrderQueryService(queryRepository, orderRepository);
        when(orderRepository.customerExists(1L)).thenReturn(true);
    }

    @Test
    void pageLoadsAllLinesInOneQueryAndReturnsCursorWhenMoreExist() {
        // size 2 → service asks for 3; repository returns 3 → there IS a next page
        when(queryRepository.findCustomerOrders(1L, null, 3)).thenReturn(List.of(
                order(30, "ORD-2003", "2026-11-19T12:00:00Z"),
                order(20, "ORD-2002", "2026-11-18T12:00:00Z"),
                order(10, "ORD-2001", "2026-11-17T12:00:00Z")));
        when(queryRepository.findLines(List.of(30L, 20L))).thenReturn(Map.of(
                30L, List.of(line("SKU-A", "10.00", 2)),
                20L, List.of(line("SKU-B", "5.00", 1))));

        OrderPage page = service.listCustomerOrders(1L, null, 2);

        assertThat(page.orders()).extracting(OrderResponse::orderNumber)
                .containsExactly("ORD-2003", "ORD-2002");               // extra row NOT shown
        assertThat(page.orders().getFirst().total()).isEqualByComparingTo("20.00");
        assertThat(page.nextCursor()).isNotNull();

        OrderCursor cursor = OrderCursor.decode(page.nextCursor());
        assertThat(cursor.id()).isEqualTo(20L);                        // bookmark = last order shown

        verify(queryRepository, times(1)).findLines(anyList());        // N+1 guard: ONE lines query
    }

    @Test
    void lastPageHasNoCursor() {
        when(queryRepository.findCustomerOrders(1L, null, 21))
                .thenReturn(List.of(order(10, "ORD-2001", "2026-11-17T12:00:00Z")));
        when(queryRepository.findLines(List.of(10L))).thenReturn(Map.of());

        OrderPage page = service.listCustomerOrders(1L, null, 20);

        assertThat(page.nextCursor()).isNull();
        assertThat(page.orders().getFirst().lines()).isEmpty();
    }

    @Test
    void unknownCustomerIs404() {
        when(orderRepository.customerExists(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> service.listCustomerOrders(99L, null, 20));
    }

    @Test
    void unknownOrderIs404() {
        when(queryRepository.findByOrderNumber("ORD-0000")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getOrder("ORD-0000"));
    }

    @Test
    void hugePageSizeIsCappedAt100() {
        when(queryRepository.findCustomerOrders(1L, null, 101)).thenReturn(List.of());

        service.listCustomerOrders(1L, null, 1_000_000);

        verify(queryRepository).findCustomerOrders(org.mockito.ArgumentMatchers.eq(1L), isNull(),
                org.mockito.ArgumentMatchers.eq(101));                 // 100 + 1 lookahead
    }
}
