package com.dosealerta.notificacao.core.usecase;

import static com.dosealerta.notificacao.NotificacaoFixtures.CLOCK_FIXO;
import static com.dosealerta.notificacao.NotificacaoFixtures.CORRELATION_ID;
import static com.dosealerta.notificacao.NotificacaoFixtures.INSTANTE_FIXO;
import static com.dosealerta.notificacao.NotificacaoFixtures.umaSolicitacaoValida;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.notificacao.TesteUnitarioBase;
import com.dosealerta.notificacao.core.domain.Canal;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;
import com.dosealerta.notificacao.core.exception.TelefoneObrigatorioException;
import com.dosealerta.notificacao.core.gateway.CorrelacaoGateway;
import com.dosealerta.notificacao.core.gateway.EstrategiaCanalGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.notificacao.core.rules.solicitarenvio.ValidadorSolicitacaoEnvioRule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

class SolicitarEnvioUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private EstrategiaCanalGateway estrategiaCanalGateway;

	@Mock
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Mock
	private CorrelacaoGateway correlacaoGateway;

	@Mock
	private ValidadorSolicitacaoEnvioRule regra;

	@Captor
	private ArgumentCaptor<OutboxEvent> eventoCaptor;

	private SolicitarEnvioUseCaseImpl useCase;
	private SolicitarEnvioInput input;

	@BeforeEach
	void setUp() {
		useCase = new SolicitarEnvioUseCaseImpl(
				estrategiaCanalGateway, outboxEventRepositoryGateway, correlacaoGateway, CLOCK_FIXO, List.of(regra));
		input = umaSolicitacaoValida();
		when(correlacaoGateway.atual()).thenReturn(CORRELATION_ID);
		when(estrategiaCanalGateway.resolverCanal(input.etapa())).thenReturn(Canal.MENSAGEM);
	}

	@Test
	void deveResolverCanalEGravarEventoDeOutboxPendenteComDataDoRelogio() {
		useCase.executar(input);

		verify(outboxEventRepositoryGateway).salvar(eventoCaptor.capture());
		OutboxEvent evento = eventoCaptor.getValue();
		assertEquals(input.alarmeId(), evento.alarmeId());
		assertEquals(input.pacienteId(), evento.pacienteId());
		assertEquals(input.telefone(), evento.telefone());
		assertEquals(input.medicamento(), evento.medicamento());
		assertEquals(input.dose(), evento.dose());
		assertEquals(input.etapa(), evento.etapa());
		assertEquals(Canal.MENSAGEM, evento.canal());
		assertEquals(INSTANTE_FIXO, evento.criadoEm());
		assertEquals(CORRELATION_ID, evento.correlationId());
	}

	@Test
	void naoDeveGravarNadaQuandoAlgumaRegraFalha() {
		doThrow(new TelefoneObrigatorioException()).when(regra).validar(any());

		assertThrows(TelefoneObrigatorioException.class, () -> useCase.executar(input));

		verifyNoInteractions(outboxEventRepositoryGateway, estrategiaCanalGateway);
	}
}
