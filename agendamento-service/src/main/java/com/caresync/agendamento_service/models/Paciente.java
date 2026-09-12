package com.caresync.agendamento_service.models;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "pacientes")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Paciente {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne 
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    private LocalDate dataNascimento;

    private String cpf;

    private String telefone;
}
