package com.dosealerta.scheduler.core.usecase;

import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.relogioEm;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarme;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.SchedulerFixtures;
import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.gateway.LogGateway;
import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import com.dosealerta.scheduler.core.rules.RegraEscalonamentoAlarme;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class EscalonarAlarmesUseCaseImplTest extends TesteUnitarioBase {

	private static final Duration INTERVALO = RegraEscalonamentoAlarme.INTERVALO_ENTRE_ETAPAS_PADRAO;

	@Mock
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	@Mock
	private MetricasAlarmeGateway metricasAlarmeGateway;

	@Mock
	private LogGateway logGateway;

	@Mock
	private CorrelacaoGateway correlacaoGateway;

	private Alarme alarme;

	@BeforeEach
	void setUp() {
		alarme = umAlarme();
		when(correlacaoGateway.atual()).thenReturn(SchedulerFixtures.CORRELATION_ID);
		when(alarmeRepositoryGateway.buscarPendentesParaEscalonamento()).thenReturn(List.of(alarme));
	}

	private EscalonarAlarmesUseCaseImpl useCaseEm(Instant agora) {
		return new EscalonarAlarmesUseCaseImpl(
				alarmeRepositoryGateway, metricasAlarmeGateway, logGateway, correlacaoGateway, relogioEm(agora), INTERVALO);
	}

	@Test
	void deveEnviarLembreteInicialQuandoChegaOHorarioAlvo() {
		useCaseEm(INSTANTE_FIXO).executar();

		assertEquals(EtapaEscalonamento.LEMBRETE_INICIAL, alarme.getEtapaAtual());
		assertEquals(1, alarme.getEventosOutbox().size());
		assertEquals(SchedulerFixtures.CORRELATION_ID, alarme.getEventosOutbox().get(0).correlationId());
		verify(alarmeRepositoryGateway).salvar(alarme);
	}

	@Test
	void naoDeveFazerNadaAntesDoHorarioAlvo() {
		useCaseEm(INSTANTE_FIXO.minusSeconds(60)).executar();

		verify(alarmeRepositoryGateway, never()).salvar(alarme);
	}

	@Test
	void deveFinalizarSemConfirmacaoAposEsgotarEscalonamento() {
		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, INSTANTE_FIXO, SchedulerFixtures.CORRELATION_ID);
		alarme.registrarEnvio(EtapaEscalonamento.REFORCO, INSTANTE_FIXO.plusSeconds(900), SchedulerFixtures.CORRELATION_ID);
		alarme.registrarEnvio(EtapaEscalonamento.LIGACAO, INSTANTE_FIXO.plusSeconds(1800), SchedulerFixtures.CORRELATION_ID);

		useCaseEm(INSTANTE_FIXO.plusSeconds(2700)).executar();

		assertEquals(StatusAlarme.NAO_CONFIRMADO, alarme.getStatus());
		verify(alarmeRepositoryGateway, times(1)).salvar(alarme);
		verify(metricasAlarmeGateway).registrarNaoConfirmacao();
	}

	@Test
	void deveRegistrarAvisoEContinuarQuandoFalhaAoSalvar() {
		doThrow(new RuntimeException("falha")).when(alarmeRepositoryGateway).salvar(any(Alarme.class));

		useCaseEm(INSTANTE_FIXO).executar();

		verify(logGateway).aviso(any(), any(), any(RuntimeException.class));
	}
}
