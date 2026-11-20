package com.shopcore.api;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderPlacementService orderPlacementService;
    private final OrderQueryService orderQueryService;

    public OrderController(OrderPlacementService orderPlacementService,
                           OrderQueryService orderQueryService) {
        this.orderPlacementService = orderPlacementService;
        this.orderQueryService = orderQueryService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse order = orderPlacementService.placeOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{orderNumber}")
                .buildAndExpand(order.orderNumber())
                .toUri();
        return ResponseEntity.created(location).body(order);
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse getOrder(@PathVariable String orderNumber) {
        return orderQueryService.getOrder(orderNumber);
    }
}