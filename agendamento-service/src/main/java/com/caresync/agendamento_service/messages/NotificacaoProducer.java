package com.caresync.agendamento_service.messages;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class NotificacaoProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${caresync.rabbitmq.exchange-name}")
    private String exchangeName;
    
    @Value("${caresync.rabbitmq.routing-key-name}")
    private String routingKey;


    public void publicar(NotificacaoConsultaEvent event) {
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
    }
}
