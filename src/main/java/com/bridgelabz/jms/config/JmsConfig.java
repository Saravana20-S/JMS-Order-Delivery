package com.bridgelabz.jms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableJms
public class JmsConfig {

    public static final String ORDER_QUEUE =
            "order.queue";

    public static final String CANCEL_QUEUE =
            "cancel.queue";

    public static final String NOTIFICATION_QUEUE =
            "notification.queue";

    public static final String ORDER_DLQ =
            "order.dlq";


    @Bean
    public ObjectMapper objectMapper() {

        return new ObjectMapper();
    }


    @Bean
    public JmsTemplate jmsTemplate(
            ConnectionFactory connectionFactory) {

        JmsTemplate jmsTemplate =
                new JmsTemplate(connectionFactory);

        jmsTemplate.setSessionTransacted(true);

        return jmsTemplate;
    }


    @Bean
    public DefaultJmsListenerContainerFactory
    jmsListenerContainerFactory(
            ConnectionFactory connectionFactory) {

        DefaultJmsListenerContainerFactory factory =
                new DefaultJmsListenerContainerFactory();

        factory.setConnectionFactory(
                connectionFactory
        );

        factory.setSessionTransacted(true);

        return factory;
    }
}