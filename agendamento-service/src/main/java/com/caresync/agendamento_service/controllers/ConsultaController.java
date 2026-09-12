package com.caresync.agendamento_service.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.caresync.agendamento_service.dto.ConsultaRequestDTO;
import com.caresync.agendamento_service.dto.ConsultaResponseDTO;
import com.caresync.agendamento_service.dto.ConsultaUpdateDTO;
import com.caresync.agendamento_service.services.ConsultaService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController 
@RequestMapping("/consultas")
@RequiredArgsConstructor 
public class ConsultaController {

    private final ConsultaService consultaService;

    @GetMapping
    public ResponseEntity<List<ConsultaResponseDTO>> listar(Authentication authentication) {
        return ResponseEntity.ok(consultaService.listarParaUsuarioLogado(authentication));
    }

    @PostMapping
    public ResponseEntity<ConsultaResponseDTO> criar(@RequestBody ConsultaRequestDTO requestDTO) {
        return ResponseEntity.ok(consultaService.criar(requestDTO));
    }
    
    @PatchMapping("{id}")
    public ResponseEntity<ConsultaResponseDTO> atualizar(@PathVariable Long id, @RequestBody ConsultaUpdateDTO requestDTO) {
        return ResponseEntity.ok(consultaService.atualizar(id, requestDTO));
    }
}
