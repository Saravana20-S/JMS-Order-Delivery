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
public class CancelProducer {

    private final JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper;

    public void sendCancelRequest(OrderMessage message) {

        try {

            String jsonMessage =
                    objectMapper.writeValueAsString(message);

            jmsTemplate.convertAndSend(
                    JmsConfig.CANCEL_QUEUE,
                    jsonMessage,
                    jmsMessage -> {

                        jmsMessage.setJMSCorrelationID(
                                message.getCorrelationId()
                        );

                        jmsMessage.setStringProperty(
                                "messageType",
                                "ORDER_CANCEL_REQUEST"
                        );

                        return jmsMessage;
                    }
            );

        } catch (JsonProcessingException e) {

            throw new RuntimeException(
                    "Failed to convert cancel request to JSON",
                    e
            );
        }
    }
}