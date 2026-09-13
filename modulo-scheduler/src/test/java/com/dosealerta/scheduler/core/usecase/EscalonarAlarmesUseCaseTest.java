package com.dosealerta.scheduler.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EscalonarAlarmesUseCaseTest {

	@Mock
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	private EscalonarAlarmesUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new EscalonarAlarmesUseCase(alarmeRepositoryGateway);
	}

	@Test
	void deveEnviarLembreteInicialQuandoChegaOHorarioAlvo() {
		Instant horarioAlvo = Instant.parse("2026-01-01T12:00:00Z");
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "Losartana", "50mg", horarioAlvo);
		when(alarmeRepositoryGateway.buscarPendentesParaEscalonamento()).thenReturn(List.of(alarme));

		useCase.executar(horarioAlvo);

		assertEquals(EtapaEscalonamento.LEMBRETE_INICIAL, alarme.getEtapaAtual());
		assertEquals(1, alarme.getEventosOutbox().size());
		verify(alarmeRepositoryGateway).salvar(alarme);
	}

	@Test
	void naoDeveFazerNadaAntesDoHorarioAlvo() {
		Instant horarioAlvo = Instant.parse("2026-01-01T12:00:00Z");
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "Losartana", "50mg", horarioAlvo);
		when(alarmeRepositoryGateway.buscarPendentesParaEscalonamento()).thenReturn(List.of(alarme));

		useCase.executar(horarioAlvo.minusSeconds(60));

		verify(alarmeRepositoryGateway, never()).salvar(alarme);
	}

	@Test
	void deveFinalizarSemConfirmacaoAposEsgotarEscalonamento() {
		Instant horarioAlvo = Instant.parse("2026-01-01T12:00:00Z");
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "Losartana", "50mg", horarioAlvo);
		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, horarioAlvo);
		alarme.registrarEnvio(EtapaEscalonamento.REFORCO, horarioAlvo.plusSeconds(900));
		alarme.registrarEnvio(EtapaEscalonamento.LIGACAO, horarioAlvo.plusSeconds(1800));
		when(alarmeRepositoryGateway.buscarPendentesParaEscalonamento()).thenReturn(List.of(alarme));

		useCase.executar(horarioAlvo.plusSeconds(2700));

		assertEquals(StatusAlarme.NAO_CONFIRMADO, alarme.getStatus());
		verify(alarmeRepositoryGateway, times(1)).salvar(alarme);
	}
}
