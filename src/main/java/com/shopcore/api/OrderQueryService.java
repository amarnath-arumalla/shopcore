package com.shopcore.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopcore.api.OrderQueryRepository.OrderRow;

@Service
public class OrderQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final OrderQueryRepository queryRepository;
    private final OrderRepository orderRepository;

    public OrderQueryService(OrderQueryRepository queryRepository, OrderRepository orderRepository) {
        this.queryRepository = queryRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNumber) {
        OrderRow order = queryRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderNumber));
        List<OrderLineResponse> lines = queryRepository.findLines(List.of(order.id()))
                .getOrDefault(order.id(), List.of());
        return toResponse(order, lines);
    }

    @Transactional(readOnly = true)
    public OrderPage listCustomerOrders(long customerId, String cursor, int size) {
        int pageSize = Math.clamp(size, 1, MAX_PAGE_SIZE);       // never trust a client's page size
        OrderCursor after = (cursor == null || cursor.isBlank()) ? null : OrderCursor.decode(cursor);

        if (!orderRepository.customerExists(customerId)) {
            throw new ResourceNotFoundException("Customer", customerId);
        }

        // ask for one extra row: if it comes back, there is a next page
        List<OrderRow> rows = queryRepository.findCustomerOrders(customerId, after, pageSize + 1);
        boolean hasMore = rows.size() > pageSize;
        List<OrderRow> page = hasMore ? rows.subList(0, pageSize) : rows;

        // N+1 fix: ONE query for the lines of every order on this page
        Map<Long, List<OrderLineResponse>> linesByOrder = page.isEmpty()
                ? Map.of()
                : queryRepository.findLines(page.stream().map(OrderRow::id).toList());

        List<OrderResponse> orders = page.stream()
                .map(o -> toResponse(o, linesByOrder.getOrDefault(o.id(), List.of())))
                .toList();

        String nextCursor = hasMore
                ? new OrderCursor(page.getLast().createdAt(), page.getLast().id()).encode()
                : null;

        return new OrderPage(orders, nextCursor);
    }

    private static OrderResponse toResponse(OrderRow order, List<OrderLineResponse> lines) {
        BigDecimal subtotal = lines.stream()
                .map(OrderLineResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderResponse(order.orderNumber(), order.customerId(), order.status(),
                subtotal, order.discount(), subtotal.subtract(order.discount()),
                order.createdAt(), lines);
    }
}
