package com.caresync.agendamento_service.messages;

public record NotificacaoConsultaEvent(
    Long consultaId,
    String pacienteNome,
    String profissionalNome,
    String dataHora,
    String tipoEvento
) {

}
