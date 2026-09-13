package com.caresync.agendamento_service.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.caresync.agendamento_service.messages.NotificacaoProducer;

import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class ConsultaControllerSecurityTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

        @Autowired
        private ObjectMapper objectMapper;

    @MockitoBean
    private NotificacaoProducer notificacaoProducer;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String criarConsulta() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"pacienteId": 1, "profissionalId": 1, "dataHora": "2026-12-06T10:00:00"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("medico1", "123456")
                .postForEntity(url("/consultas"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String consultaId = objectMapper.readTree(response.getBody()).path("id").asText();
        assertThat(consultaId).isNotBlank();
        return consultaId;
    }

    @Test
    void medico_conseguelistarConsultas() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("medico1", "123456")
                .getForEntity(url("/consultas"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void paciente_conseguelistarConsultas() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paciente1", "123456")
                .getForEntity(url("/consultas"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void paciente_naoConsegueCriarConsulta_retorna403() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"pacienteId": 1, "profissionalId": 1, "dataHora": "2026-12-01T10:00:00"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paciente1", "123456")
                .postForEntity(url("/consultas"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void semAutenticacao_retorna401() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/consultas"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void medico_criarConsultaSemPacienteId_retorna400() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"profissionalId": 1, "dataHora": "2026-12-01T10:00:00"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("medico1", "123456")
                .postForEntity(url("/consultas"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void medico_editarConsultaInexistente_retorna404() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"status": "CANCELADA"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("medico1", "123456")
                .exchange(url("/consultas/99999"), org.springframework.http.HttpMethod.PATCH,
                        new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void enfermeiro_consegueListarConsultas() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("enfermeiro1", "123456")
                .getForEntity(url("/consultas"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void enfermeiro_consegueCriarConsulta() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"pacienteId": 1, "profissionalId": 1, "dataHora": "2026-12-02T10:00:00"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("enfermeiro1", "123456")
                .postForEntity(url("/consultas"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void medico_consegueEditarConsulta() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String consultaId = criarConsulta();

        String updateBody = """
                {"dataHora": "2026-12-10T14:30:00", "observacoes": "Consulta remarcada", "status": "CONCLUIDA"}
                """;

        ResponseEntity<String> updateResponse = restTemplate
                .withBasicAuth("medico1", "123456")
                .exchange(url("/consultas/" + consultaId), org.springframework.http.HttpMethod.PATCH,
                        new HttpEntity<>(updateBody, headers), String.class);

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).contains("Consulta remarcada");
        assertThat(updateResponse.getBody()).contains("CONCLUIDA");
    }

    @Test
    void paciente_naoConseguePatchConsulta_retorna403() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String consultaId = criarConsulta();
        String body = """
                {"status": "CANCELADA"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paciente1", "123456")
                .exchange(url("/consultas/" + consultaId), org.springframework.http.HttpMethod.PATCH,
                        new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void medico_consegueCriarConsultaViaMutationGraphQL() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"query": "mutation { criarConsulta(pacienteId: 1, profissionalId: 1, dataHora: \\"2026-12-03T10:00:00\\") { id status } }"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("medico1", "123456")
                .postForEntity(url("/graphql"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("errors");
        assertThat(response.getBody()).contains("criarConsulta");
    }

    @Test
    void paciente_naoConsegueCriarConsultaViaMutationGraphQL() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"query": "mutation { criarConsulta(pacienteId: 1, profissionalId: 1, dataHora: \\"2026-12-04T10:00:00\\") { id status } }"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paciente1", "123456")
                .postForEntity(url("/graphql"), new HttpEntity<>(body, headers), String.class);

        // GraphQL retorna 200 no transporte HTTP mesmo quando a operação
        // falha - o erro vem estruturado dentro do corpo da resposta.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("errors");
        assertThat(response.getBody()).containsIgnoringCase("forbidden");
    }

    @Test
    void paciente_consegueConsultarProprioHistoricoViaQueryGraphQL() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"query": "{ consultasPorPaciente(pacienteId: 1) { id pacienteNome profissionalNome dataHora status observacoes } }"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paciente1", "123456")
                .postForEntity(url("/graphql"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("errors");
        assertThat(response.getBody()).contains("consultasPorPaciente");
    }

    @Test
    void medico_consegueConsultarConsultasFuturasViaQueryGraphQL() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"query": "{ consultasFuturas(pacienteId: 1) { id pacienteNome profissionalNome dataHora status } }"}
                """;

        ResponseEntity<String> response = restTemplate
                .withBasicAuth("medico1", "123456")
                .postForEntity(url("/graphql"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("errors");
        assertThat(response.getBody()).contains("consultasFuturas");
    }

        @Test
        void medico_podeAtualizarConsultaViaMutationGraphQL() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
                String consultaId = criarConsulta();
        String body = """
                                {"query": "mutation { atualizarConsulta(id: %s, status: \\"CANCELADA\\") { id status } }"}
                                """.formatted(consultaId);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("medico1", "123456")
                .postForEntity(url("/graphql"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).doesNotContain("errors");
        assertThat(response.getBody()).contains("atualizarConsulta");
    }

        @Test
        void paciente_naoConsegueAtualizarConsultaViaMutationGraphQL() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
                String consultaId = criarConsulta();
        String body = """
                                {"query": "mutation { atualizarConsulta(id: %s, status: \\"CANCELADA\\") { id status } }"}
                                """.formatted(consultaId);
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("paciente1", "123456")
                .postForEntity(url("/graphql"), new HttpEntity<>(body, headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("errors");
        assertThat(response.getBody()).containsIgnoringCase("forbidden");
    }
}