package com.caresync.agendamento_service.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.caresync.agendamento_service.securities.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration 
@EnableWebSecurity 
@EnableMethodSecurity 
@RequiredArgsConstructor 
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationManager authenticationManger(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/h2-console/**", "/graphiql/**").permitAll()
            // Só MEDICO e ENFERMEIRO podem criar/editar consultas
            .requestMatchers(HttpMethod.POST, "/consultas/**").hasAnyRole("MEDICO", "ENFERMEIRO")
            .requestMatchers(HttpMethod.PATCH, "/consultas/**").hasAnyRole("MEDICO", "ENFERMEIRO")
            // Leitura de consultas: os 3 perfis podem acessar
            // (o Service/Controller decide o que cada role pode VER de fato)
            .requestMatchers(HttpMethod.GET, "/consultas/**").hasAnyRole("MEDICO", "ENFERMEIRO", "PACIENTE")
            .requestMatchers("/graphql/**").hasAnyRole("MEDICO", "ENFERMEIRO", "PACIENTE")
            .anyRequest().authenticated()
            ).httpBasic(basic -> {})
            // Necessário para o iframe do H2 Console funcionar
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));


        return http.build();
    }
}
