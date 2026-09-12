package com.caresync.agendamento_service.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caresync.agendamento_service.models.enums.Consulta;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPacienteId(Long pacienteId);

    List<Consulta> findByPacienteIdAndDataHoraAfter(Long pacienteId, LocalDateTime agora);

    List<Consulta> findByPacienteUsuarioLogin(String login);
}
