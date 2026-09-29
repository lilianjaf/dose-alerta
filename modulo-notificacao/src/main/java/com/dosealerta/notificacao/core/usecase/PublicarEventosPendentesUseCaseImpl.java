package com.dosealerta.notificacao.core.usecase;

import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.gateway.CorrelacaoGateway;
import com.dosealerta.notificacao.core.gateway.LogGateway;
import com.dosealerta.notificacao.core.gateway.MensageriaClientGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class PublicarEventosPendentesUseCaseImpl implements PublicarEventosPendentesUseCase {

	static final int MAX_TENTATIVAS = 5;
	static final Duration ESPERA_INICIAL = Duration.ofSeconds(30);
	static final Duration VALIDADE = Duration.ofMinutes(30);

	private static final int TAMANHO_LOTE = 50;
	private static final String MENSAGEM_EVENTO_EXPIRADO =
			"Evento de outbox {} expirou sem ser enviado (criado em {}), descartando";
	private static final String MENSAGEM_TENTATIVAS_ESGOTADAS =
			"Evento de outbox {} falhou após {} tentativas, não será mais retentado";
	private static final String MENSAGEM_NOVA_TENTATIVA =
			"Falha ao publicar o evento de outbox {} (tentativa {}/{}), próxima em {}";

	private final OutboxEventRepositoryGateway outboxEventRepositoryGateway;
	private final MensageriaClientGateway mensageriaClientGateway;
	private final LogGateway logGateway;
	private final CorrelacaoGateway correlacaoGateway;
	private final Clock clock;

	public PublicarEventosPendentesUseCaseImpl(OutboxEventRepositoryGateway outboxEventRepositoryGateway, MensageriaClientGateway mensageriaClientGateway, LogGateway logGateway, CorrelacaoGateway correlacaoGateway, Clock clock) {
		this.outboxEventRepositoryGateway = outboxEventRepositoryGateway;
		this.mensageriaClientGateway = mensageriaClientGateway;
		this.logGateway = logGateway;
		this.correlacaoGateway = correlacaoGateway;
		this.clock = clock;
	}

	@Override
	public void executar() {
		Instant agora = clock.instant();
		outboxEventRepositoryGateway.buscarPendentes(TAMANHO_LOTE, agora).forEach(evento ->
				correlacaoGateway.executarCom(evento.correlationId(), () -> publicar(evento, agora)));
	}

	private void publicar(OutboxEvent evento, Instant agora) {
		if (evento.criadoEm().plus(VALIDADE).isBefore(agora)) {
			expirar(evento);
			return;
		}
		try {
			mensageriaClientGateway.enviar(evento);
			outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), agora);
		} catch (RuntimeException e) {
			tratarFalha(evento, agora, e);
		}
	}

	private void expirar(OutboxEvent evento) {
		logGateway.aviso(MENSAGEM_EVENTO_EXPIRADO, evento.id(), evento.criadoEm());
		outboxEventRepositoryGateway.marcarComoExpirado(evento.id());
	}

	private void tratarFalha(OutboxEvent evento, Instant agora, RuntimeException e) {
		int tentativas = evento.tentativas() + 1;
		if (tentativas >= MAX_TENTATIVAS) {
			logGateway.erro(MENSAGEM_TENTATIVAS_ESGOTADAS, evento.id(), tentativas, e);
			outboxEventRepositoryGateway.marcarComoFalhou(evento.id(), tentativas);
			return;
		}
		Instant proxima = agora.plus(ESPERA_INICIAL.multipliedBy(1L << (tentativas - 1)));
		logGateway.aviso(MENSAGEM_NOVA_TENTATIVA, evento.id(), tentativas, MAX_TENTATIVAS, proxima, e);
		outboxEventRepositoryGateway.registrarFalha(evento.id(), tentativas, proxima);
	}
}
