package com.bridgelabz.jms.consumer;

import com.bridgelabz.jms.config.JmsConfig;
import com.bridgelabz.jms.dto.NotificationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final ObjectMapper objectMapper;


    @JmsListener(
            destination = JmsConfig.NOTIFICATION_QUEUE,
            containerFactory = "jmsListenerContainerFactory"
    )
    public void processNotification(
            String message) {

        try {

            NotificationMessage notificationMessage =
                    objectMapper.readValue(
                            message,
                            NotificationMessage.class
                    );


            System.out.println(
                    "===================================="
            );

            System.out.println(
                    "Notification Message Received"
            );

            System.out.println(
                    "Order ID: "
                            + notificationMessage.getOrderId()
            );

            System.out.println(
                    "Customer ID: "
                            + notificationMessage.getCustomerId()
            );

            System.out.println(
                    "Message: "
                            + notificationMessage.getMessage()
            );

            System.out.println(
                    "Correlation ID: "
                            + notificationMessage.getCorrelationId()
            );

            System.out.println(
                    "===================================="
            );


            // Notification business logic

            sendNotification(notificationMessage);

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to convert notification JSON",
                    e
            );
        }
    }


    private void sendNotification(
            NotificationMessage message) {

        System.out.println(
                "Sending notification to customer "
                        + message.getCustomerId()
        );

        System.out.println(
                "Notification: "
                        + message.getMessage()
        );
    }
}