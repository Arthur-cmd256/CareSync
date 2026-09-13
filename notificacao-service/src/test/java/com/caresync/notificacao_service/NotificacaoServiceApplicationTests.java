package com.caresync.notificacao_service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.caresync.notificacao_service.messages.NotificacaoConsultaEvent;
import com.caresync.notificacao_service.messages.NotificacaoListener;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NotificacaoServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void processarNotificacao_quandoConsultaCriada_emiteLembreteAgendado() {
		String saida = executarECapturarSaida(new NotificacaoConsultaEvent(
				10L, "Carlos Lima", "Dra. Ana Souza", "2026-12-10T14:30", "CRIADA"));

		assertThat(saida).contains(
				"[LEMBRETE] Consulta #10 agendada! Paciente: Carlos Lima | "
						+ "Profissional: Dra. Ana Souza | Data/Hora: 2026-12-10T14:30");
	}

	@Test
	void processarNotificacao_quandoConsultaAtualizada_emiteLembreteAtualizado() {
		String saida = executarECapturarSaida(new NotificacaoConsultaEvent(
				11L, "Maria Santos", "Dr. Paulo Lima", "2026-12-11T09:00", "ATUALIZADA"));

		assertThat(saida).contains(
				"[LEMBRETE] Consulta #11 atualizada! Paciente: Maria Santos | "
						+ "Profissional: Dr. Paulo Lima | Data/Hora: 2026-12-11T09:00");
	}

	private String executarECapturarSaida(NotificacaoConsultaEvent evento) {
		PrintStream saidaOriginal = System.out;
		ByteArrayOutputStream saidaCapturada = new ByteArrayOutputStream();

		try {
			System.setOut(new PrintStream(saidaCapturada));
			new NotificacaoListener().processarNotificacao(evento);
			return saidaCapturada.toString();
		} finally {
			System.setOut(saidaOriginal);
		}
	}

}
