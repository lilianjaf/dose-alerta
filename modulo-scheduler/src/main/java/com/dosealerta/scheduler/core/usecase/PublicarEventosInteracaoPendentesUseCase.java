package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.EventoInteracao;
import com.dosealerta.scheduler.core.gateway.EventoInteracaoRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.RelatorioAdesaoClientGateway;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publisher assíncrono do Outbox de interações (Etapa 8): lê os eventos pendentes e aciona o
 * modulo-relatorio-adesao. Mesma técnica de Transactional Outbox já usada para o outbox de
 * escalonamento — se a publicação falhar, o evento permanece pendente para nova tentativa.
 */
public class PublicarEventosInteracaoPendentesUseCase {

	private static final Logger log = LoggerFactory.getLogger(PublicarEventosInteracaoPendentesUseCase.class);
	private static final int TAMANHO_LOTE = 50;

	private final EventoInteracaoRepositoryGateway eventoInteracaoRepositoryGateway;
	private final RelatorioAdesaoClientGateway relatorioAdesaoClientGateway;

	public PublicarEventosInteracaoPendentesUseCase(
			EventoInteracaoRepositoryGateway eventoInteracaoRepositoryGateway,
			RelatorioAdesaoClientGateway relatorioAdesaoClientGateway) {
		this.eventoInteracaoRepositoryGateway = eventoInteracaoRepositoryGateway;
		this.relatorioAdesaoClientGateway = relatorioAdesaoClientGateway;
	}

	public void executar() {
		for (EventoInteracao evento : eventoInteracaoRepositoryGateway.buscarPendentes(TAMANHO_LOTE)) {
			try {
				relatorioAdesaoClientGateway.registrarInteracao(evento);
				eventoInteracaoRepositoryGateway.marcarComoPublicado(evento.id(), Instant.now());
			} catch (RuntimeException e) {
				log.warn("Falha ao publicar o evento de interação {}, será retentado no próximo ciclo", evento.id(), e);
			}
		}
	}
}
