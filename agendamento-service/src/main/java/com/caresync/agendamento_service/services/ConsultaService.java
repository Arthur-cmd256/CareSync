package com.caresync.agendamento_service.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.caresync.agendamento_service.dto.ConsultaGraphQLDTO;
import com.caresync.agendamento_service.dto.ConsultaRequestDTO;
import com.caresync.agendamento_service.dto.ConsultaResponseDTO;
import com.caresync.agendamento_service.dto.ConsultaUpdateDTO;
import com.caresync.agendamento_service.mappers.ConsultaMapper;
import com.caresync.agendamento_service.messages.NotificacaoConsultaEvent;
import com.caresync.agendamento_service.messages.NotificacaoProducer;
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
    private final NotificacaoProducer notificacaoProducer;

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

            Consulta consultaSalva = consultaRepository.save(consulta);

            notificacaoProducer.publicar(new NotificacaoConsultaEvent(
                consultaSalva.getId(),
                consultaSalva.getPaciente().getUsuario().getNome(),
                consultaSalva.getProfissional().getUsuario().getNome(),
                consultaSalva.getDataHora().toString(),
                "CRIADA"
            ));

        return consultaMapper.toResponseDTO(consultaRepository.save(consultaSalva));
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

        Consulta consultaAtualizada = consultaRepository.save(consulta);

        notificacaoProducer.publicar(new NotificacaoConsultaEvent(
            consultaAtualizada.getId(),
            consultaAtualizada.getPaciente().getUsuario().getNome(),
            consultaAtualizada.getProfissional().getUsuario().getNome(),
            consultaAtualizada.getDataHora().toString(),
            "ATUALIZADA"
        ));

        return consultaMapper.toResponseDTO(consultaAtualizada);
    }

    public List<ConsultaGraphQLDTO>  listarPorPaciente(Long pacienteId, Authentication authentication) {
        validarAcessoAoPaciente(pacienteId, authentication);

        return consultaRepository.findByPacienteId(pacienteId).stream()
            .map(consultaMapper::toGraphQLDTO)
            .toList();
    }

    public List<ConsultaGraphQLDTO> listarFuturasPorPaciente(Long pacienteId, Authentication authentication) {
        validarAcessoAoPaciente(pacienteId, authentication);

        return consultaRepository.findByPacienteIdAndDataHoraAfter(pacienteId, LocalDateTime.now()).stream()
                .map(consultaMapper::toGraphQLDTO)
                .toList();
    }


    private void validarAcessoAoPaciente(Long pacienteId, Authentication authentication) {
        boolean ehPaciente = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_PACIENTE"));

        if (!ehPaciente) {
            return; // MEDICO/ENFERMEIRO podem consultar qualquer paciente
        }

        Paciente pacienteLogado = pacienteRepository.findByUsuarioLogin(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Paciente autenticado não encontrado"));
                
        if (!pacienteLogado.getId().equals(pacienteId)) {
            throw new AccessDeniedException("Paciente só pode consultar o próprio histórico");
        }
    }
}
