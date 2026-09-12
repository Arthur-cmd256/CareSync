package com.caresync.agendamento_service.dto;

import java.time.LocalDateTime;

import com.caresync.agendamento_service.models.enums.StatusConsulta;

public record ConsultaUpdateDTO(
    LocalDateTime dataHora,
    String observacoes,
    StatusConsulta status
) {

}
