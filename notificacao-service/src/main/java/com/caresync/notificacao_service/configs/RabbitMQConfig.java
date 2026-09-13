package com.caresync.notificacao_service.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration 
public class RabbitMQConfig {

    @Value("${caresync.rabbitmq.queue-name}")
    private String queueName;

    @Value("${caresync.rabbitmq.exchange-name}")
    private String exchangeName;

    @Value("${caresync.rabbitmq.routing-key-name}")
    private String routingKey;
    

    
    @Bean
    public Queue notificacoesQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange consultasExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue notificacoesQueue, DirectExchange consultasExchange) {
        return BindingBuilder.bind(notificacoesQueue).to(consultasExchange).with(routingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
