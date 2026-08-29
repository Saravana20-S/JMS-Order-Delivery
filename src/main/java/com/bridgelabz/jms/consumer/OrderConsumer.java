
package com.bridgelabz.jms.consumer;

import com.bridgelabz.jms.config.JmsConfig;
import com.bridgelabz.jms.dto.NotificationMessage;
import com.bridgelabz.jms.dto.OrderMessage;
import com.bridgelabz.jms.entity.Order;
import com.bridgelabz.jms.entity.OrderEvent;
import com.bridgelabz.jms.enums.EventType;
import com.bridgelabz.jms.enums.OrderStatus;
import com.bridgelabz.jms.producer.NotificationProducer;
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
public class OrderConsumer {

    private final OrderRepository orderRepository;

    private final OrderEventRepository orderEventRepository;

    private final NotificationProducer notificationProducer;

    private final ObjectMapper objectMapper;


    @JmsListener(
            destination = JmsConfig.ORDER_QUEUE,
            containerFactory = "jmsListenerContainerFactory"
    )
    @Transactional
    public void processOrder(String message) {

        try {

            // -----------------------------------------
            // Convert JSON message to OrderMessage
            // -----------------------------------------

            OrderMessage orderMessage =
                    objectMapper.readValue(
                            message,
                            OrderMessage.class
                    );


            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "Received Order Message"
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


            // -----------------------------------------
            // Find Order
            // -----------------------------------------

            Order order =
                    orderRepository.findById(
                            orderMessage.getOrderId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Order not found with id: "
                                            + orderMessage.getOrderId()
                            )
                    );


            // -----------------------------------------
            // PROCESSING
            // -----------------------------------------

            order.setStatus(
                    OrderStatus.PROCESSING
            );

            orderRepository.save(order);

            saveEvent(
                    order,
                    EventType.ORDER_PROCESSING,
                    "Order processing started"
            );


            // -----------------------------------------
            // INVENTORY
            // -----------------------------------------

            checkInventory(order);

            saveEvent(
                    order,
                    EventType.INVENTORY_CHECKED,
                    "Inventory checked successfully"
            );


            // -----------------------------------------
            // PAYMENT
            // -----------------------------------------

            processPayment(order);

            saveEvent(
                    order,
                    EventType.PAYMENT_COMPLETED,
                    "Payment completed successfully"
            );


            // -----------------------------------------
            // COMPLETE ORDER
            // -----------------------------------------

            order.setStatus(
                    OrderStatus.COMPLETED
            );

            orderRepository.save(order);

            saveEvent(
                    order,
                    EventType.ORDER_COMPLETED,
                    "Order completed successfully"
            );


            // -----------------------------------------
            // SEND NOTIFICATION
            // -----------------------------------------

            NotificationMessage notification =
                    NotificationMessage.builder()
                            .orderId(order.getId())
                            .customerId(
                                    order.getCustomerId()
                            )
                            .message(
                                    "Your order "
                                            + order.getId()
                                            + " has been completed successfully."
                            )
                            .correlationId(
                                    order.getCorrelationId()
                            )
                            .build();

            notificationProducer.sendNotification(
                    notification
            );


            System.out.println(
                    "Order "
                            + order.getId()
                            + " COMPLETED"
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to convert JMS JSON message to OrderMessage",
                    e
            );
        }
    }


    // -----------------------------------------
    // Inventory Processing
    // -----------------------------------------

    private void checkInventory(
            Order order) {

        System.out.println(
                "Checking inventory..."
        );

//        throw new RuntimeException(
//                "Inventory unavailable - testing failure"
//        );


        System.out.println(
                "Product ID: "
                        + order.getProductId()
        );

        System.out.println(
                "Quantity: "
                        + order.getQuantity()
        );


    }


    // -----------------------------------------
    // Payment Processing
    // -----------------------------------------

    private void processPayment(
            Order order) {

        System.out.println(
                "Processing payment..."
        );

        System.out.println(
                "Customer ID: "
                        + order.getCustomerId()
        );
    }


    // -----------------------------------------
    // Save Order Event
    // -----------------------------------------

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

