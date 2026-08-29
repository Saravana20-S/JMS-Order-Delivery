package com.bridgelabz.jms.producer;

import com.bridgelabz.jms.config.JmsConfig;
import com.bridgelabz.jms.dto.NotificationMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper;

    public void sendNotification(
            NotificationMessage message) {

        try {

            String jsonMessage =
                    objectMapper.writeValueAsString(
                            message
                    );

            jmsTemplate.convertAndSend(
                    JmsConfig.NOTIFICATION_QUEUE,
                    jsonMessage,
                    jmsMessage -> {

                        jmsMessage.setJMSCorrelationID(
                                message.getCorrelationId()
                        );

                        jmsMessage.setStringProperty(
                                "messageType",
                                "ORDER_NOTIFICATION"
                        );

                        return jmsMessage;
                    }
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to convert notification to JSON",
                    e
            );
        }
    }
}