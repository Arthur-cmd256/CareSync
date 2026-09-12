package com.caresync.agendamento_service.services;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.caresync.agendamento_service.dto.ConsultaRequestDTO;
import com.caresync.agendamento_service.dto.ConsultaResponseDTO;
import com.caresync.agendamento_service.dto.ConsultaUpdateDTO;
import com.caresync.agendamento_service.mappers.ConsultaMapper;
import com.caresync.agendamento_service.models.Paciente;
import com.caresync.agendamento_service.models.Profissional;
import com.caresync.agendamento_service.models.enums.Consulta;
import com.caresync.agendamento_service.models.enums.StatusConsulta;
import com.caresync.agendamento_service.repositories.ConsultaRepository;
import com.caresync.agendamento_service.repositories.PacienteRepository;
import com.caresync.agendamento_service.repositories.ProfissionalRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final ConsultaMapper consultaMapper;

    public List<ConsultaResponseDTO> listarParaUsuarioLogado(Authentication authentication) {
        boolean ehPaciente = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_PACIENTE"));

        List<Consulta> consultas;
        if (ehPaciente) {
            String login = authentication.getName();
            consultas = consultaRepository.findByPacienteUsuarioLogin(login);
        } else {
            consultas = consultaRepository.findAll();
        }
        return consultas.stream()
                .map(consultaMapper::toResponseDTO)
                .toList();
    }

    public ConsultaResponseDTO criar(ConsultaRequestDTO requestDTO) {
        Paciente paciente = pacienteRepository.findById(requestDTO.pacienteId())
            .orElseThrow(() -> new RuntimeException("Paciente não encontrado: " + requestDTO.pacienteId()));

            Profissional profissional = profissionalRepository.findById(requestDTO.profissionalId())
            .orElseThrow(() -> new RuntimeException("Profissional não encontrado: " + requestDTO.profissionalId()));

            Consulta consulta = Consulta.builder()
                .paciente(paciente)
                .profissional(profissional)
                .dataHora(requestDTO.dataHora())
                .observacoes(requestDTO.observacoes())
                .status(StatusConsulta.AGENDADA)
                .build();

        return consultaMapper.toResponseDTO(consultaRepository.save(consulta));
    }

    public ConsultaResponseDTO atualizar(Long id, ConsultaUpdateDTO requestDTO) {
        Consulta consulta = consultaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Consulta não encontrada: " + id));

        if (requestDTO.dataHora() != null) {
            consulta.setDataHora(requestDTO.dataHora());
        }
        if (requestDTO.observacoes() != null) {
            consulta.setObservacoes(requestDTO.observacoes ());
        }
        if (requestDTO.status() != null) {
            consulta.setStatus(requestDTO.status());
        }


        return consultaMapper.toResponseDTO(consultaRepository.save(consulta));
    }
}
