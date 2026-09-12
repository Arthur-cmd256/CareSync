package com.caresync.agendamento_service.controllers;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.caresync.agendamento_service.dto.ConsultaGraphQLDTO;
import com.caresync.agendamento_service.services.ConsultaService;

import lombok.RequiredArgsConstructor;

@Controller 
@RequiredArgsConstructor 
public class ConsultaGraphQLController {
    private final ConsultaService consultaService;

    @QueryMapping
    public List<ConsultaGraphQLDTO> consultasPorPaciente(@Argument Long pacienteId, Authentication authentication) {
        return consultaService.listarPorPaciente(pacienteId, authentication);
    }

    @QueryMapping 
    public List<ConsultaGraphQLDTO> consultasFuturas(@Argument Long pacienteId, Authentication authentication) {
        return consultaService.listarFuturasPorPaciente(pacienteId, authentication);
    }
}
