package com.bridgelabz.jms.service;

import com.bridgelabz.jms.dto.CreateOrderRequest;
import com.bridgelabz.jms.entity.Order;
import com.bridgelabz.jms.entity.OrderEvent;

import java.util.List;

public interface OrderService {

    Order createOrder(CreateOrderRequest request);

    Order getOrder(Long id);

    void cancelOrder(Long id);

    void retryOrder(Long id);

    List<OrderEvent> getOrderEvents(Long id);

    List<Order> getFailedOrders();
}