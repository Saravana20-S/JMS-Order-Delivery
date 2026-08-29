package com.bridgelabz.jms.producer;

import com.bridgelabz.jms.config.JmsConfig;
import com.bridgelabz.jms.dto.OrderMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProducer {

    private final JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper;

    public void sendOrder(OrderMessage message) {

        try {

            // Convert OrderMessage object to JSON
            String jsonMessage =
                    objectMapper.writeValueAsString(message);

            System.out.println(
                    "Sending Order Message: "
                            + jsonMessage
            );

            jmsTemplate.convertAndSend(
                    JmsConfig.ORDER_QUEUE,
                    jsonMessage,
                    jmsMessage -> {

                        jmsMessage.setJMSCorrelationID(
                                message.getCorrelationId()
                        );

                        jmsMessage.setStringProperty(
                                "messageType",
                                "ORDER_CREATED"
                        );

                        return jmsMessage;
                    }
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to convert OrderMessage to JSON",
                    e
            );
        }
    }
}