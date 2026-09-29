package com.dosealerta.notificacao.core.usecase;

import static com.dosealerta.notificacao.NotificacaoFixtures.CORRELATION_ID;
import static com.dosealerta.notificacao.NotificacaoFixtures.INSTANTE_FIXO;
import static com.dosealerta.notificacao.NotificacaoFixtures.comTentativas;
import static com.dosealerta.notificacao.NotificacaoFixtures.umEventoOutboxCriadoEm;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.notificacao.NotificacaoFixtures;
import com.dosealerta.notificacao.TesteUnitarioBase;
import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.exception.MensageriaIndisponivelException;
import com.dosealerta.notificacao.core.gateway.CorrelacaoGateway;
import com.dosealerta.notificacao.core.gateway.LogGateway;
import com.dosealerta.notificacao.core.gateway.MensageriaClientGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class PublicarEventosPendentesUseCaseImplTest extends TesteUnitarioBase {

	private static final int TAMANHO_LOTE = 50;
	private static final Duration DEZ_SEGUNDOS = Duration.ofSeconds(10);
	private static final Duration TRES_HORAS = Duration.ofHours(3);
	private static final Duration VINTE_E_NOVE_MINUTOS = Duration.ofMinutes(29);
	private static final Duration DOIS_MINUTOS = Duration.ofMinutes(2);

	@Mock
	private OutboxEventRepositoryGateway outboxEventRepositoryGateway;

	@Mock
	private MensageriaClientGateway mensageriaClientGateway;

	@Mock
	private LogGateway logGateway;

	@Mock
	private CorrelacaoGateway correlacaoGateway;

	private PublicarEventosPendentesUseCaseImpl useCase;

	@BeforeEach
	void setUp() {
		useCase = new PublicarEventosPendentesUseCaseImpl(
				outboxEventRepositoryGateway,
				mensageriaClientGateway,
				logGateway,
				correlacaoGateway,
				NotificacaoFixtures.CLOCK_FIXO);
		doAnswer(inv -> {
					((Runnable) inv.getArgument(1)).run();
					return null;
				})
				.when(correlacaoGateway)
				.executarCom(eq(CORRELATION_ID), any(Runnable.class));
	}

	private void pendentes(OutboxEvent... eventos) {
		when(outboxEventRepositoryGateway.buscarPendentes(TAMANHO_LOTE, INSTANTE_FIXO)).thenReturn(List.of(eventos));
	}

	private OutboxEvent recente() {
		return umEventoOutboxCriadoEm(INSTANTE_FIXO.minus(DEZ_SEGUNDOS));
	}

	@Test
	void devePublicarEventoPendenteEMarcarComoPublicado() {
		OutboxEvent evento = recente();
		pendentes(evento);

		useCase.executar();

		verify(mensageriaClientGateway).enviar(evento);
		verify(outboxEventRepositoryGateway).marcarComoPublicado(evento.id(), INSTANTE_FIXO);
	}

	@Test
	void deveAgendarNovaTentativaComEsperaCrescenteQuandoAMensageriaFalha() {
		OutboxEvent primeiraFalha = recente();
		OutboxEvent terceiraFalha = comTentativas(recente(), 2);
		pendentes(primeiraFalha, terceiraFalha);
		doThrow(new MensageriaIndisponivelException("falhou", null)).when(mensageriaClientGateway).enviar(any());

		useCase.executar();

		verify(outboxEventRepositoryGateway)
				.registrarFalha(primeiraFalha.id(), 1, INSTANTE_FIXO.plus(PublicarEventosPendentesUseCaseImpl.ESPERA_INICIAL));
		verify(outboxEventRepositoryGateway).registrarFalha(terceiraFalha.id(), 3, INSTANTE_FIXO.plus(DOIS_MINUTOS));
		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(any(UUID.class), any(Instant.class));
		verify(outboxEventRepositoryGateway, never()).marcarComoFalhou(any(UUID.class), anyInt());
	}

	@Test
	void devePararDeTentarQuandoEsgotaAsTentativas() {
		OutboxEvent ultimaChance = comTentativas(recente(), PublicarEventosPendentesUseCaseImpl.MAX_TENTATIVAS - 1);
		pendentes(ultimaChance);
		doThrow(new MensageriaIndisponivelException("falhou", null)).when(mensageriaClientGateway).enviar(ultimaChance);

		useCase.executar();

		verify(outboxEventRepositoryGateway)
				.marcarComoFalhou(ultimaChance.id(), PublicarEventosPendentesUseCaseImpl.MAX_TENTATIVAS);
		verify(outboxEventRepositoryGateway, never()).registrarFalha(any(UUID.class), anyInt(), any(Instant.class));
	}

	@Test
	void deveDescartarSemEnviarOEventoVelhoDemais() {
		OutboxEvent velho = umEventoOutboxCriadoEm(INSTANTE_FIXO.minus(TRES_HORAS));
		pendentes(velho);

		useCase.executar();

		verify(mensageriaClientGateway, never()).enviar(any());
		verify(outboxEventRepositoryGateway).marcarComoExpirado(velho.id());
		verify(outboxEventRepositoryGateway, never()).marcarComoPublicado(eq(velho.id()), any(Instant.class));
	}

	@Test
	void naoDeveExpirarEventoDentroDaValidade() {
		OutboxEvent dentroDaValidade = umEventoOutboxCriadoEm(INSTANTE_FIXO.minus(VINTE_E_NOVE_MINUTOS));
		pendentes(dentroDaValidade);

		useCase.executar();

		verify(mensageriaClientGateway).enviar(dentroDaValidade);
		verify(outboxEventRepositoryGateway, never()).marcarComoExpirado(any(UUID.class));
	}
}
