package com.dosealerta.notificacao.core.usecase;

import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.gateway.MensageriaClientGateway;
import com.dosealerta.notificacao.core.gateway.OutboxEventRepositoryGateway;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class PublicarEventosPendentesUseCase {

	private static final Logger log = LoggerFactory.getLogger(PublicarEventosPendentesUseCase.class);
	private static final int TAMANHO_LOTE = 50;
	private static final String MDC_CORRELATION_ID_KEY = "correlationId";

	static final int MAX_TENTATIVAS = 5;
	static final Duration ESPERA_INICIAL = Duration.ofSeconds(30);
	// Um lembrete mais velho que isso já foi superado pelas etapas seguintes do escalonamento (a cada 15 min).
	static final Duration VALIDADE = Duration.ofMinutes(30);

	private final OutboxEventRepositoryGateway outboxEventRepositoryGateway;
	private final MensageriaClientGateway mensageriaClientGateway;

	public PublicarEventosPendentesUseCase(
			OutboxEventRepositoryGateway outboxEventRepositoryGateway, MensageriaClientGateway mensageriaClientGateway) {
		this.outboxEventRepositoryGateway = outboxEventRepositoryGateway;
		this.mensageriaClientGateway = mensageriaClientGateway;
	}

	public void executar() {
		executar(Instant.now());
	}

	/**
	 * Publica os eventos pendentes cuja hora de tentar já chegou. Falhas são retentadas com espera crescente
	 * (30s, 1min, 2min, 4min) até {@value #MAX_TENTATIVAS} tentativas; depois o evento vira FALHOU. Eventos mais
	 * velhos que {@code VALIDADE} são descartados (EXPIRADO) sem envio.
	 */
	void executar(Instant agora) {
		for (OutboxEvent evento : outboxEventRepositoryGateway.buscarPendentes(TAMANHO_LOTE, agora)) {
			MDC.put(MDC_CORRELATION_ID_KEY, evento.correlationId());
			try {
				publicar(evento, agora);
			} finally {
				MDC.remove(MDC_CORRELATION_ID_KEY);
			}
		}
	}

	private void publicar(OutboxEvent evento, Instant agora) {
		if (evento.criadoEm().plus(VALIDADE).isBefore(agora)) {
			log.warn("Evento de outbox {} expirou sem ser enviado (criado em {}), descartando", evento.id(), evento.criadoEm());
			outboxEventRepositoryGateway.marcarComoExpirado(evento.id());
			return;
		}

		try {
			mensageriaClientGateway.enviar(evento);
			outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), agora);
		} catch (RuntimeException e) {
			int tentativas = evento.tentativas() + 1;
			if (tentativas >= MAX_TENTATIVAS) {
				log.error("Evento de outbox {} falhou após {} tentativas, não será mais retentado", evento.id(), tentativas, e);
				outboxEventRepositoryGateway.marcarComoFalhou(evento.id(), tentativas);
				return;
			}
			Instant proxima = agora.plus(ESPERA_INICIAL.multipliedBy(1L << (tentativas - 1)));
			log.warn("Falha ao publicar o evento de outbox {} (tentativa {}/{}), próxima em {}",
					evento.id(), tentativas, MAX_TENTATIVAS, proxima, e);
			outboxEventRepositoryGateway.registrarFalha(evento.id(), tentativas, proxima);
		}
	}
}
