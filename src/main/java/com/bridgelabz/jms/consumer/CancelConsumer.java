package com.bridgelabz.jms.consumer;

import com.bridgelabz.jms.config.JmsConfig;
import com.bridgelabz.jms.dto.OrderMessage;
import com.bridgelabz.jms.entity.Order;
import com.bridgelabz.jms.entity.OrderEvent;
import com.bridgelabz.jms.enums.EventType;
import com.bridgelabz.jms.enums.OrderStatus;
import com.bridgelabz.jms.repository.OrderEventRepository;
import com.bridgelabz.jms.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CancelConsumer {

    private final OrderRepository orderRepository;

    private final OrderEventRepository orderEventRepository;

    private final ObjectMapper objectMapper;


    @JmsListener(
            destination = JmsConfig.CANCEL_QUEUE,
            containerFactory = "jmsListenerContainerFactory"
    )
    @Transactional
    public void cancelOrder(String message) {

        try {

            OrderMessage orderMessage =
                    objectMapper.readValue(
                            message,
                            OrderMessage.class
                    );

            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "Received Cancel Message"
            );

            System.out.println(
                    "Order ID: "
                            + orderMessage.getOrderId()
            );

            System.out.println(
                    "Correlation ID: "
                            + orderMessage.getCorrelationId()
            );

            System.out.println(
                    "===================================="
            );


            Order order =
                    orderRepository.findById(
                            orderMessage.getOrderId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Order not found with ID: "
                                            + orderMessage.getOrderId()
                            )
                    );


            // --------------------------------
            // Check current order status
            // --------------------------------

            if (order.getStatus()
                    == OrderStatus.CANCELLED) {

                System.out.println(
                        "Order is already cancelled."
                );

                return;
            }


            if (order.getStatus()
                    == OrderStatus.COMPLETED) {

                System.out.println(
                        "Completed order cannot be cancelled."
                );

                saveEvent(
                        order,
                        EventType.ORDER_CANCEL_FAILED,
                        "Completed order cannot be cancelled"
                );

                return;
            }


            // --------------------------------
            // Cancel Order
            // --------------------------------

            order.setStatus(
                    OrderStatus.CANCELLED
            );

            orderRepository.save(order);


            // --------------------------------
            // Save Event
            // --------------------------------

            saveEvent(
                    order,
                    EventType.ORDER_CANCELLED,
                    "Order cancelled successfully"
            );


            System.out.println(
                    "Order "
                            + order.getId()
                            + " CANCELLED"
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to convert cancel message from JSON",
                    e
            );
        }
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