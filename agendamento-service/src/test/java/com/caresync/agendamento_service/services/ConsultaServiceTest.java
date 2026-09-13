package com.caresync.agendamento_service.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.caresync.agendamento_service.dto.ConsultaGraphQLDTO;
import com.caresync.agendamento_service.dto.ConsultaRequestDTO;
import com.caresync.agendamento_service.dto.ConsultaResponseDTO;
import com.caresync.agendamento_service.dto.ConsultaUpdateDTO;
import com.caresync.agendamento_service.mappers.ConsultaMapper;
import com.caresync.agendamento_service.messages.NotificacaoProducer;
import com.caresync.agendamento_service.models.Paciente;
import com.caresync.agendamento_service.models.Profissional;
import com.caresync.agendamento_service.models.Usuario;
import com.caresync.agendamento_service.models.enums.Consulta;
import com.caresync.agendamento_service.models.enums.Role;
import com.caresync.agendamento_service.models.enums.StatusConsulta;
import com.caresync.agendamento_service.repositories.ConsultaRepository;
import com.caresync.agendamento_service.repositories.PacienteRepository;
import com.caresync.agendamento_service.repositories.ProfissionalRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ConsultaServiceTest {
    @Mock private ConsultaRepository consultaRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private ProfissionalRepository profissionalRepository;
    @Mock private NotificacaoProducer notificacaoProducer;


    private final ConsultaMapper consultaMapper = new ConsultaMapper();
    private ConsultaService consultaService;
 
    private Paciente paciente;
    private Profissional profissional;
    private Consulta consulta;
 
    @BeforeEach
    void setUp() {
        consultaService = new ConsultaService(
                consultaRepository, pacienteRepository, profissionalRepository,
                consultaMapper, notificacaoProducer
        );

        
        Usuario usuarioPaciente = Usuario.builder()
                .id(1L).nome("Carlos Lima").login("paciente1").role(Role.PACIENTE).build();
        paciente = Paciente.builder().id(1L).usuario(usuarioPaciente).build();
 
        Usuario usuarioMedico = Usuario.builder()
                .id(2L).nome("Dra. Ana Souza").login("medico1").role(Role.MEDICO).build();
        profissional = Profissional.builder().id(1L).usuario(usuarioMedico).build();
 
        consulta = Consulta.builder()
                .id(1L)
                .paciente(paciente)
                .profissional(profissional)
                .dataHora(LocalDateTime.now().plusDays(1))
                .status(StatusConsulta.AGENDADA)
                .build();
    }

    private Authentication authenticationComRole(String login, Role role) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
        return new UsernamePasswordAuthenticationToken(login, null, authorities);
    }

    @Test
    void listarParaUsuarioLogado_quandoPaciente_retornaApenasAsProprias() {
        Authentication auth = authenticationComRole("paciente1", Role.PACIENTE);
        when(consultaRepository.findByPacienteUsuarioLogin(anyString()))
                .thenReturn(List.of(consulta));
 
        List<ConsultaResponseDTO> resultado = consultaService.listarParaUsuarioLogado(auth);
 
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(consulta.getId());
        assertThat(resultado.get(0).pacienteNome()).isEqualTo("Carlos Lima");
        verify(consultaRepository, times(1)).findByPacienteUsuarioLogin("paciente1");
        verify(consultaRepository, never()).findAll();
    }

    @Test
    void listarParaUsuarioLogado_quandoMedico_retornaTodasAsConsultas() {
        Authentication auth = authenticationComRole("medico1", Role.MEDICO);
        when(consultaRepository.findAll()).thenReturn(List.of(consulta));
 
        List<ConsultaResponseDTO> resultado = consultaService.listarParaUsuarioLogado(auth);
 
        assertThat(resultado).hasSize(1);
        verify(consultaRepository, times(1)).findAll();
        verify(consultaRepository, never()).findByPacienteUsuarioLogin(any());
    }


    @Test
    void criar_devePersistirEPublicarNotificacao() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(
            1L, 
            1L, 
            LocalDateTime.now().plusDays(2), 
            "obs"
        );
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consulta);
 
        ConsultaResponseDTO resultado = consultaService.criar(dto);
 
        assertThat(resultado.pacienteNome()).isEqualTo("Carlos Lima");
        verify(consultaRepository, times(1)).save(any(Consulta.class));
        verify(notificacaoProducer, times(1)).publicar(any());
    }

    @Test
    void criar_quandoPacienteNaoExiste_lancaExcecao() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(
            999L, 
            1L, 
            LocalDateTime.now().plusDays(1), 
            null
        );
        when(pacienteRepository.findById(999L)).thenReturn(Optional.empty());
 
        assertThatThrownBy(() -> consultaService.criar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Paciente não encontrado");
 
        verify(consultaRepository, never()).save(any());
        verify(notificacaoProducer, never()).publicar(any());
    }

    @Test
    void criar_quandoProfissionalNaoExiste_lancaExcecao() {
        ConsultaRequestDTO dto = new ConsultaRequestDTO(
            1L, 
            999L, 
            LocalDateTime.now().plusDays(1), 
            null
        );
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(profissionalRepository.findById(999L)).thenReturn(Optional.empty());
 
        assertThatThrownBy(() -> consultaService.criar(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Profissional não encontrado");
 
        verify(consultaRepository, never()).save(any());
        verify(notificacaoProducer, never()).publicar(any());
    }

    @Test
    void atualizar_quandoApenasStatusInformado_preservaDemaisCampos() {
        LocalDateTime dataOriginal = consulta.getDataHora();
        ConsultaUpdateDTO dto = new ConsultaUpdateDTO(null, null, StatusConsulta.CANCELADA);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consulta);
 
        consultaService.atualizar(1L, dto);
 
        assertThat(consulta.getDataHora()).isEqualTo(dataOriginal);
        assertThat(consulta.getStatus()).isEqualTo(StatusConsulta.CANCELADA);
    }

    @Test
    void atualizar_quandoArgumentosOpcionaisNulos_preservaTodosOsCampos() {
        LocalDateTime dataOriginal = consulta.getDataHora();
        String observacoesOriginais = consulta.getObservacoes();
        StatusConsulta statusOriginal = consulta.getStatus();
        ConsultaUpdateDTO dto = new ConsultaUpdateDTO(null, null, null);
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(consultaRepository.save(any(Consulta.class))).thenReturn(consulta);

        consultaService.atualizar(1L, dto);

        assertThat(consulta.getDataHora()).isEqualTo(dataOriginal);
        assertThat(consulta.getObservacoes()).isEqualTo(observacoesOriginais);
        assertThat(consulta.getStatus()).isEqualTo(statusOriginal);
        verify(notificacaoProducer, times(1)).publicar(any());
    }

    @Test
    void atualizar_quandoConsultaNaoExiste_lancaExcecao() {
        when(consultaRepository.findById(999L)).thenReturn(Optional.empty());
        ConsultaUpdateDTO dto = new ConsultaUpdateDTO(null, null, StatusConsulta.CANCELADA);
 
        assertThatThrownBy(() -> consultaService.atualizar(999L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Consulta não encontrada");
    }

    @Test
    void listarPorPaciente_quandoPacienteTentaVerHistoricoDeOutro_lancaAccessDenied() {
        Authentication auth = authenticationComRole("paciente1", Role.PACIENTE);
        when(pacienteRepository.findByUsuarioLogin("paciente1")).thenReturn(Optional.of(paciente));
 
        assertThatThrownBy(() -> consultaService.listarPorPaciente(2L, auth))
                .isInstanceOf(AccessDeniedException.class);
 
        verify(consultaRepository, never()).findByPacienteId(any());
    }

    @Test
    void listarPorPaciente_quandoMedicoConsultaQualquerPaciente_permiteAcesso() {
        Authentication auth = authenticationComRole("medico1", Role.MEDICO);
        when(consultaRepository.findByPacienteId(2L)).thenReturn(List.of());
 
        List<ConsultaGraphQLDTO> resultado = consultaService.listarPorPaciente(2L, auth);
 
        assertThat(resultado).isEmpty();
        verify(pacienteRepository, never()).findByUsuarioLogin(any());
    }


    @Test
    void listarFuturasPorPaciente_quandoMedicoConsultaQualquerPaciente_permiteAcesso() {
        Authentication auth = authenticationComRole("medico1", Role.MEDICO);
        when(consultaRepository.findByPacienteIdAndDataHoraAfter(anyLong(), any(LocalDateTime.class))).thenReturn(List.of());
 
        List<ConsultaGraphQLDTO> resultado = consultaService.listarFuturasPorPaciente(2L, auth);
 
        assertThat(resultado).isEmpty();
        verify(pacienteRepository, never()).findByUsuarioLogin(any());
    }
}
