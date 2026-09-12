package com.caresync.agendamento_service.mappers;

import org.springframework.stereotype.Component;

import com.caresync.agendamento_service.dto.ConsultaGraphQLDTO;
import com.caresync.agendamento_service.dto.ConsultaResponseDTO;
import com.caresync.agendamento_service.models.enums.Consulta;

@Component 
public class ConsultaMapper {

    public ConsultaResponseDTO toResponseDTO(Consulta consulta) {
        return new ConsultaResponseDTO(
            consulta.getId(),
            consulta.getPaciente().getUsuario().getNome(),
            consulta.getProfissional().getUsuario().getNome(),
            consulta.getDataHora(),
            consulta.getStatus(),
            consulta.getObservacoes()
        );
    }

    public ConsultaGraphQLDTO toGraphQLDTO(Consulta consulta) {
        return new ConsultaGraphQLDTO(
                consulta.getId(),
                consulta.getPaciente().getUsuario().getNome(),
                consulta.getProfissional().getUsuario().getNome(),
                consulta.getDataHora().toString(),
                consulta.getStatus().name(),
                consulta.getObservacoes()
        );
    }

}
