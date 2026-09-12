package com.caresync.agendamento_service.dto;

public record ConsultaGraphQLDTO(
    Long id,
    String pacienteNome,
    String profissionalNome,
    String dataHora,
    String status,
    String observacoes
) {

}
