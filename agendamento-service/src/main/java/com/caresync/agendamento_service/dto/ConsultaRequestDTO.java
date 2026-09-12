package com.caresync.agendamento_service.dto;

import java.time.LocalDateTime;

public record ConsultaRequestDTO(
    Long pacienteId,
    Long profissionalId,
    LocalDateTime dataHora,
    String observacoes
) {

}
