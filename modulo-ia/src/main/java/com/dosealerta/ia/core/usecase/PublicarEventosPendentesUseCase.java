package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.OutboxEvent;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publisher assíncrono do Outbox (Etapa 7.4): lê os {@code ReceitaConfirmadaEvent} pendentes
 * e aciona o modulo-scheduler para criar o alarme da primeira dose. Mesma técnica de
 * Transactional Outbox já usada em modulo-scheduler e modulo-notificacao — se a publicação
 * falhar, o evento permanece pendente para nova tentativa no próximo ciclo.
 */
public class PublicarEventosPendentesUseCase {

	private static final Logger log = LoggerFactory.getLogger(PublicarEventosPendentesUseCase.class);
	private static final int TAMANHO_LOTE = 50;

	private final ReceitaRepositoryGateway receitaRepositoryGateway;
	private final OutboxEventRepositoryGateway outboxEventRepositoryGateway;
	private final SchedulerClientGateway schedulerClientGateway;

	public PublicarEventosPendentesUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway,
			OutboxEventRepositoryGateway outboxEventRepositoryGateway,
			SchedulerClientGateway schedulerClientGateway) {
		this.receitaRepositoryGateway = receitaRepositoryGateway;
		this.outboxEventRepositoryGateway = outboxEventRepositoryGateway;
		this.schedulerClientGateway = schedulerClientGateway;
	}

	public void executar() {
		for (OutboxEvent evento : outboxEventRepositoryGateway.buscarPendentes(TAMANHO_LOTE)) {
			try {
				publicar(evento);
			} catch (RuntimeException e) {
				log.warn("Falha ao publicar o evento de outbox {}, será retentado no próximo ciclo", evento.id(), e);
			}
		}
	}

	private void publicar(OutboxEvent evento) {
		receitaRepositoryGateway.buscarPorId(evento.receitaId()).ifPresent(receita -> {
			schedulerClientGateway.criarAlarme(
					receita.getPacienteId(), receita.getTelefone(), receita.getMedicamento(), receita.getDose(),
					receita.getHorarioInicial());
			outboxEventRepositoryGateway.marcarComoPublicado(evento.id(), Instant.now());
		});
	}
}
