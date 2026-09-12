package com.caresync.agendamento_service.configs;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.caresync.agendamento_service.models.Paciente;
import com.caresync.agendamento_service.models.Profissional;
import com.caresync.agendamento_service.models.Usuario;
import com.caresync.agendamento_service.models.enums.Role;
import com.caresync.agendamento_service.repositories.PacienteRepository;
import com.caresync.agendamento_service.repositories.ProfissionalRepository;
import com.caresync.agendamento_service.repositories.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component 
@RequiredArgsConstructor 
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return; // evita duplicar em caso de reload
        }
        
        // ---- Médico ----
        Usuario usuarioMedico = usuarioRepository.save(Usuario.builder()
                .nome("Dra. Ana Souza")
                .login("medico1")
                .senha(passwordEncoder.encode("123456"))
                .role(Role.MEDICO)
                .build());

        profissionalRepository.save(Profissional.builder()
                .usuario(usuarioMedico)
                .especialidade("Clínica Geral")
                .registroProfissional("CRM-12345")
                .build());

        // ---- Enfermeiro ----
        Usuario usuarioEnfermeiro = usuarioRepository.save(Usuario.builder()
                .nome("João Pereira")
                .login("enfermeiro1")
                .senha(passwordEncoder.encode("123456"))
                .role(Role.ENFERMEIRO)
                .build());

        profissionalRepository.save(Profissional.builder()
                .usuario(usuarioEnfermeiro)
                .especialidade(null)
                .registroProfissional("COREN-67890")
                .build());

        // ---- Paciente ----
        Usuario usuarioPaciente = usuarioRepository.save(Usuario.builder()
                .nome("Carlos Lima")
                .login("paciente1")
                .senha(passwordEncoder.encode("123456"))
                .role(Role.PACIENTE)
                .build());

        pacienteRepository.save(Paciente.builder()
                .usuario(usuarioPaciente)
                .dataNascimento(LocalDate.of(1990, 5, 20))
                .cpf("111.222.333-44")
                .telefone("(11) 99999-0000")
                .build());
                
        System.out.println("=== Seed de dados criado: medico1 / enfermeiro1 / paciente1 (senha: 123456) ===");
    }

}
