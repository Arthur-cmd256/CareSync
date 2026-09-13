package com.caresync.agendamento_service.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record ConsultaRequestDTO(

    @NotNull(message = "pacienteId é obrigatório") 
    Long pacienteId,
    @NotNull(message = "profissionalId é obrigatório")
    Long profissionalId,
    @NotNull(message = "dataHora é obrigatória")
    @Future(message = "dataHora deve ser uma data futura")
    LocalDateTime dataHora,
    String observacoes
) {

}
