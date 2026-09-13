package com.caresync.agendamento_service.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;

@Configuration 
public class RabbitMQConfig {
    
    @Value("${caresync.rabbitmq.queue-name}")
    private String queueName;

    @Value("${caresync.rabbitmq.exchange-name}")
    private String exchangeName;
    
    @Value("${caresync.rabbitmq.routing-key-name}")
    private String routingKey;


    @Bean 
    public Queue notificationQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange consultaExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue notificationQueue, DirectExchange consultaExchange) {
        return BindingBuilder.bind(notificationQueue).to(consultaExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

}
