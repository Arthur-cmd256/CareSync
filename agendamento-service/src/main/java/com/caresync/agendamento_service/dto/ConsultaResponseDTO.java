package com.caresync.agendamento_service.dto;

import java.time.LocalDateTime;

import com.caresync.agendamento_service.models.enums.StatusConsulta;

public record ConsultaResponseDTO(
    Long id,
    String pacienteNome,
    String profissionalNome,
    LocalDateTime dataHora,
    StatusConsulta status,
    String observacoes
) {
    
}
