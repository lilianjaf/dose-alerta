package com.dosealerta.scheduler.core.usecase;

import static com.dosealerta.scheduler.SchedulerFixtures.CLOCK_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.CORRELATION_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.TELEFONE;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarmeEnviado;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import com.dosealerta.scheduler.core.exception.AlarmePendenteNaoEncontradoException;
import com.dosealerta.scheduler.core.exception.ConflitoConcorrenciaException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.gateway.MetricasAlarmeGateway;
import com.dosealerta.scheduler.core.rules.registrarconfirmacao.ValidadorRegistroConfirmacaoRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class RegistrarConfirmacaoUseCaseImplTest extends TesteUnitarioBase {

	private static final int MAX_TENTATIVAS = 3;

	@Mock
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	@Mock
	private MetricasAlarmeGateway metricasAlarmeGateway;

	@Mock
	private CorrelacaoGateway correlacaoGateway;

	@Mock
	private ValidadorRegistroConfirmacaoRule regra;

	private RegistrarConfirmacaoUseCaseImpl useCase;
	private Alarme alarme;

	@BeforeEach
	void setUp() {
		useCase = new RegistrarConfirmacaoUseCaseImpl(
				alarmeRepositoryGateway, metricasAlarmeGateway, correlacaoGateway, CLOCK_FIXO, List.of(regra));
		alarme = umAlarmeEnviado(EtapaEscalonamento.LEMBRETE_INICIAL);
		when(correlacaoGateway.atual()).thenReturn(CORRELATION_ID);
		when(alarmeRepositoryGateway.salvar(any(Alarme.class))).thenAnswer(inv -> inv.getArgument(0));
	}

	private void pendenteEncontrado() {
		when(alarmeRepositoryGateway.buscarPendenteMaisRecentePorTelefone(TELEFONE)).thenReturn(Optional.of(alarme));
	}

	private void sempreUmNovoPendente() {
		when(alarmeRepositoryGateway.buscarPendenteMaisRecentePorTelefone(TELEFONE))
				.thenAnswer(inv -> Optional.of(umAlarmeEnviado(EtapaEscalonamento.LEMBRETE_INICIAL)));
	}

	private ConflitoConcorrenciaException conflito() {
		return new ConflitoConcorrenciaException(SchedulerFixtures.ALARME_ID, new RuntimeException("conflito"));
	}

	@Test
	void deveRegistrarParaOAlarmePendenteMaisRecentePorTelefone() {
		pendenteEncontrado();

		Alarme resultado = useCase.executar(TELEFONE);

		assertEquals(StatusAlarme.CONFIRMADO, resultado.getStatus());
		verify(alarmeRepositoryGateway).salvar(alarme);
		verify(metricasAlarmeGateway).registrarConfirmacao();
	}

	@Test
	void naoDeveSalvarQuandoAlgumaRegraFalha() {
		when(alarmeRepositoryGateway.buscarPendenteMaisRecentePorTelefone(TELEFONE)).thenReturn(Optional.empty());
		doThrow(new AlarmePendenteNaoEncontradoException(TELEFONE)).when(regra).validar(any());

		assertThrows(AlarmePendenteNaoEncontradoException.class, () -> useCase.executar(TELEFONE));

		verify(alarmeRepositoryGateway, never()).salvar(any());
	}

	@Test
	void deveTentarNovamenteAposConflitoDeConcorrenciaEConseguir() {
		sempreUmNovoPendente();
		when(alarmeRepositoryGateway.salvar(any(Alarme.class))).thenThrow(conflito()).thenAnswer(inv -> inv.getArgument(0));

		Alarme resultado = useCase.executar(TELEFONE);

		assertEquals(StatusAlarme.CONFIRMADO, resultado.getStatus());
		verify(alarmeRepositoryGateway, times(2)).salvar(any(Alarme.class));
	}

	@Test
	void deveDesistirAposEsgotarAsTentativas() {
		sempreUmNovoPendente();
		when(alarmeRepositoryGateway.salvar(any(Alarme.class))).thenThrow(conflito());

		assertThrows(ConflitoConcorrenciaException.class, () -> useCase.executar(TELEFONE));

		verify(alarmeRepositoryGateway, times(MAX_TENTATIVAS)).salvar(any(Alarme.class));
	}

	@Test
	void naoDeveConsultarOGatewayQuandoOTelefoneEEmBranco() {
		doThrow(new AlarmePendenteNaoEncontradoException(SchedulerFixtures.VALOR_EM_BRANCO)).when(regra).validar(any());

		assertThrows(
				AlarmePendenteNaoEncontradoException.class, () -> useCase.executar(SchedulerFixtures.VALOR_EM_BRANCO));

		verify(alarmeRepositoryGateway, never()).buscarPendenteMaisRecentePorTelefone(any());
	}
}
