package com.caresync.agendamento_service.controllers;

import com.caresync.agendamento_service.mappers.ConsultaMapper;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import com.caresync.agendamento_service.dto.ConsultaGraphQLDTO;
import com.caresync.agendamento_service.dto.ConsultaRequestDTO;
import com.caresync.agendamento_service.dto.ConsultaUpdateDTO;
import com.caresync.agendamento_service.models.enums.StatusConsulta;
import com.caresync.agendamento_service.services.ConsultaService;

import lombok.RequiredArgsConstructor;

@Controller 
@RequiredArgsConstructor 
public class ConsultaGraphQLController {
    private final ConsultaMapper consultaMapper;
    private final ConsultaService consultaService;

    @QueryMapping
    public List<ConsultaGraphQLDTO> consultasPorPaciente(@Argument Long pacienteId, Authentication authentication) {
        return consultaService.listarPorPaciente(pacienteId, authentication);
    }

    @QueryMapping 
    public List<ConsultaGraphQLDTO> consultasFuturas(@Argument Long pacienteId, Authentication authentication) {
        return consultaService.listarFuturasPorPaciente(pacienteId, authentication);
    }

    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    @MutationMapping
    public ConsultaGraphQLDTO criarConsulta(
            @Argument Long pacienteId,
            @Argument Long profissionalId,
            @Argument String dataHora,
            @Argument String observacoes) {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(
                pacienteId,
                profissionalId,
                LocalDateTime.parse(dataHora),
                observacoes
        );
        return consultaMapper.toGraphQLDTO(consultaService.criar(dto));
    }
 
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO')")
    @MutationMapping
    public ConsultaGraphQLDTO atualizarConsulta(
            @Argument Long id,
            @Argument String dataHora,
            @Argument String observacoes,
            @Argument String status) {
        ConsultaUpdateDTO dto = new ConsultaUpdateDTO(
                dataHora != null ? LocalDateTime.parse(dataHora) : null,
                observacoes,
                status != null ? StatusConsulta.valueOf(status) : null
        );
        return consultaMapper.toGraphQLDTO(consultaService.atualizar(id, dto));
    }

}
