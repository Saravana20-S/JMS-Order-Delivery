package com.bridgelabz.jms.controller;

import com.bridgelabz.jms.dto.CreateOrderRequest;
import com.bridgelabz.jms.entity.Order;
import com.bridgelabz.jms.entity.OrderEvent;
import com.bridgelabz.jms.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ==========================================
    // CREATE ORDER
    // POST /orders
    // ==========================================

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @Valid
            @RequestBody
            CreateOrderRequest request) {

        Order order = orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(order);
    }

    // ==========================================
    // FAILED ORDERS
    // GET /orders/failed
    // ==========================================

    @GetMapping("/failed")
    public ResponseEntity<List<Order>> getFailedOrders() {

        return ResponseEntity.ok(
                orderService.getFailedOrders()
        );
    }

    // ==========================================
    // GET ORDER
    // GET /orders/{id}
    // ==========================================

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.getOrder(id)
        );
    }

    // ==========================================
    // CANCEL ORDER
    // POST /orders/{id}/cancel
    // ==========================================

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long id) {

        orderService.cancelOrder(id);

        return ResponseEntity
                .accepted()
                .body(
                        "Order cancellation request accepted"
                );
    }

    // ==========================================
    // RETRY ORDER
    // POST /orders/{id}/retry
    // ==========================================

    @PostMapping("/{id}/retry")
    public ResponseEntity<String> retryOrder(@PathVariable Long id) {

        orderService.retryOrder(id);

        return ResponseEntity
                .accepted()
                .body(
                        "Order retry request accepted"
                );
    }

    // ==========================================
    // ORDER EVENTS
    // GET /orders/{id}/events
    // ==========================================

    @GetMapping("/{id}/events")
    public ResponseEntity<List<OrderEvent>> getOrderEvents(@PathVariable Long id) {

        return ResponseEntity.ok(
                orderService.getOrderEvents(id)
        );
    }

}