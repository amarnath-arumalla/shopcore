package com.shopcore.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerOrderController {

    private final OrderQueryService orderQueryService;

    public CustomerOrderController(OrderQueryService orderQueryService) {
        this.orderQueryService = orderQueryService;
    }

    @GetMapping("/customers/{customerId}/orders")
    public OrderPage listOrders(@PathVariable long customerId,
                                @RequestParam(required = false) String cursor,
                                @RequestParam(defaultValue = "20") int size) {
        return orderQueryService.listCustomerOrders(customerId, cursor, size);
    }
}
