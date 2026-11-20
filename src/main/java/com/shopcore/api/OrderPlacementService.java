package com.shopcore.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopcore.api.OrderRepository.CreatedOrder;

@Service
public class OrderPlacementService {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacementService.class);

    private final OrderRepository orderRepository;
    private final ProductCatalogRepository productRepository;

    public OrderPlacementService(OrderRepository orderRepository,
                                 ProductCatalogRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * Everything below runs on ONE connection in ONE database transaction.
     * Normal return → COMMIT. Any RuntimeException → ROLLBACK (stock, order, lines all undone).
     */
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        List<OrderLineRequest> lines = sortedWithoutDuplicates(request.lines());

        if (!orderRepository.customerExists(request.customerId())) {
            throw new CustomerNotFoundException(request.customerId());
        }

        CreatedOrder order = orderRepository.insertOrder(request.customerId());

        List<OrderLineResponse> lineResponses = new ArrayList<>();
        for (OrderLineRequest line : lines) {                       // sorted by SKU = same lock order for everyone
            StockReservation reservation = productRepository
                    .reserveStock(line.sku(), line.quantity())
                    .orElseThrow(() -> explainWhyReservationFailed(line));

            orderRepository.insertLine(order.id(), reservation, line.quantity());

            BigDecimal lineTotal = reservation.unitPrice().multiply(BigDecimal.valueOf(line.quantity()));
            lineResponses.add(new OrderLineResponse(line.sku(), reservation.name(),
                    reservation.unitPrice(), line.quantity(), lineTotal));
        }

        BigDecimal subtotal = lineResponses.stream()
                .map(OrderLineResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = subtotal.subtract(order.discount());

        log.info("Order {} placed for customer {} with {} lines, total {}",
                order.orderNumber(), request.customerId(), lineResponses.size(), total);

        return new OrderResponse(order.orderNumber(), request.customerId(), order.status(),
                subtotal, order.discount(), total, order.createdAt(), lineResponses);
    }

    /** 0 rows updated: unknown SKU or too little stock? Look once, only to pick the right error. */
    private RuntimeException explainWhyReservationFailed(OrderLineRequest line) {
        return productRepository.findBySku(line.sku())
                .<RuntimeException>map(p -> new InsufficientStockException(line.sku(), line.quantity(), p.stock()))
                .orElseGet(() -> new ProductNotFoundException(line.sku()));
    }

    private static List<OrderLineRequest> sortedWithoutDuplicates(List<OrderLineRequest> lines) {
        Set<String> seen = new HashSet<>();
        for (OrderLineRequest line : lines) {
            if (!seen.add(line.sku())) {
                throw new InvalidOrderException("SKU '" + line.sku()
                        + "' appears more than once. Put the total quantity on one line.");
            }
        }
        return lines.stream()
                .sorted(Comparator.comparing(OrderLineRequest::sku))
                .toList();
    }
}
