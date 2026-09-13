package com.caresync.notificacao_service.messages;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component 
public class NotificacaoListener {

    @RabbitListener(queues = "${caresync.rabbitmq.queue-name}")
    public void processarNotificacao(NotificacaoConsultaEvent evento) {
        String acao = "CRIADA".equals(evento.tipoEvento())
                ? "agendada"
                : "atualizada";

        System.out.printf(
                "[LEMBRETE] Consulta #%d %s! Paciente: %s | Profissional: %s | Data/Hora: %s%n",
                evento.consultaId(),
                acao,
                evento.pacienteNome(),
                evento.profissionalNome(),
                evento.dataHora()
        );

        // Aqui entraria a integração real (e-mail, SMS, push notification).
        // Para o desafio, o log acima já demonstra o processamento assíncrono
        // da mensagem recebida do agendamento-service.
    }

}
