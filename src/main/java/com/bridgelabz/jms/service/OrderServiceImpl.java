package com.bridgelabz.jms.service;

import com.bridgelabz.jms.dto.CreateOrderRequest;
import com.bridgelabz.jms.dto.OrderMessage;
import com.bridgelabz.jms.entity.Order;
import com.bridgelabz.jms.entity.OrderEvent;
import com.bridgelabz.jms.enums.EventType;
import com.bridgelabz.jms.enums.OrderStatus;
import com.bridgelabz.jms.exception.OrderNotFoundException;
import com.bridgelabz.jms.producer.CancelProducer;
import com.bridgelabz.jms.producer.OrderProducer;
import com.bridgelabz.jms.repository.OrderEventRepository;
import com.bridgelabz.jms.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderEventRepository orderEventRepository;

    private final OrderProducer orderProducer;

    private final CancelProducer cancelProducer;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        String correlationId = UUID.randomUUID().toString();

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .status(OrderStatus.CREATED)
                .retryCount(0)
                .correlationId(correlationId)
                .build();

        Order savedOrder = orderRepository.save(order);

        saveEvent(savedOrder,
                EventType.ORDER_CREATED,
                "Order created successfully"
        );

        OrderMessage message = OrderMessage.builder()
                        .orderId(savedOrder.getId())
                        .customerId(
                                savedOrder.getCustomerId()
                        )
                        .productId(
                                savedOrder.getProductId()
                        )
                        .quantity(
                                savedOrder.getQuantity()
                        )
                        .correlationId(
                                savedOrder.getCorrelationId()
                        )
                        .build();

        orderProducer.sendOrder(message);

        return savedOrder;
    }

    @Override
    public Order getOrder(Long id) {

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found with id: " + id)
                );
    }

    @Override
    @Transactional
    public void cancelOrder(Long id) {

        Order order = getOrder(id);

        order.setStatus(
                OrderStatus.CANCEL_REQUESTED
        );

        orderRepository.save(order);

        saveEvent(
                order,
                EventType.ORDER_CANCEL_REQUESTED,
                "Order cancellation requested"
        );

        OrderMessage message =
                OrderMessage.builder()
                        .orderId(order.getId())
                        .customerId(order.getCustomerId())
                        .productId(order.getProductId())
                        .quantity(order.getQuantity())
                        .correlationId(
                                order.getCorrelationId()
                        )
                        .build();

        cancelProducer.sendCancelRequest(message);
    }

    @Override
    @Transactional
    public void retryOrder(Long id) {

        Order order = getOrder(id);

        if (order.getStatus()
                != OrderStatus.FAILED) {

            throw new IllegalStateException(
                    "Only FAILED orders can be retried"
            );
        }

        order.setRetryCount(
                order.getRetryCount() + 1
        );

        order.setStatus(OrderStatus.CREATED);

        orderRepository.save(order);

        saveEvent(
                order,
                EventType.ORDER_RETRIED,
                "Retry attempt: "
                        + order.getRetryCount()
        );

        OrderMessage message =
                OrderMessage.builder()
                        .orderId(order.getId())
                        .customerId(order.getCustomerId())
                        .productId(order.getProductId())
                        .quantity(order.getQuantity())
                        .correlationId(
                                order.getCorrelationId()
                        )
                        .build();

        orderProducer.sendOrder(message);
    }

    @Override
    public List<OrderEvent> getOrderEvents(
            Long id) {

        getOrder(id);

        return orderEventRepository
                .findByOrderIdOrderByCreatedAtAsc(id);
    }

    @Override
    public List<Order> getFailedOrders() {

        return orderRepository.findByStatus(
                OrderStatus.FAILED
        );
    }

    private void saveEvent(
            Order order,
            EventType eventType,
            String message) {

        OrderEvent event =
                OrderEvent.builder()
                        .orderId(order.getId())
                        .eventType(eventType)
                        .message(message)
                        .correlationId(
                                order.getCorrelationId()
                        )
                        .build();

        orderEventRepository.save(event);
    }
}