package com.dosealerta.scheduler.core.usecase;

import com.dosealerta.scheduler.core.domain.EventoInteracao;
import com.dosealerta.scheduler.core.gateway.CorrelacaoGateway;
import com.dosealerta.scheduler.core.gateway.EventoInteracaoRepositoryGateway;
import com.dosealerta.scheduler.core.gateway.LogGateway;
import com.dosealerta.scheduler.core.gateway.RelatorioAdesaoClientGateway;
import java.time.Clock;

public class PublicarEventosInteracaoPendentesUseCaseImpl implements PublicarEventosInteracaoPendentesUseCase {

	private static final int TAMANHO_LOTE = 50;
	private static final String MENSAGEM_FALHA_PUBLICACAO =
			"Falha ao publicar o evento de interação {}, será retentado no próximo ciclo";

	private final EventoInteracaoRepositoryGateway eventoInteracaoRepositoryGateway;
	private final RelatorioAdesaoClientGateway relatorioAdesaoClientGateway;
	private final LogGateway logGateway;
	private final CorrelacaoGateway correlacaoGateway;
	private final Clock clock;

	public PublicarEventosInteracaoPendentesUseCaseImpl(EventoInteracaoRepositoryGateway eventoInteracaoRepositoryGateway, RelatorioAdesaoClientGateway relatorioAdesaoClientGateway, LogGateway logGateway, CorrelacaoGateway correlacaoGateway, Clock clock) {
		this.eventoInteracaoRepositoryGateway = eventoInteracaoRepositoryGateway;
		this.relatorioAdesaoClientGateway = relatorioAdesaoClientGateway;
		this.logGateway = logGateway;
		this.correlacaoGateway = correlacaoGateway;
		this.clock = clock;
	}

	@Override
	public void executar() {
		eventoInteracaoRepositoryGateway.buscarPendentes(TAMANHO_LOTE).forEach(evento ->
				correlacaoGateway.executarCom(evento.correlationId(), () -> publicarComTolerancia(evento)));
	}

	private void publicarComTolerancia(EventoInteracao evento) {
		try {
			relatorioAdesaoClientGateway.registrarInteracao(evento);
			eventoInteracaoRepositoryGateway.marcarComoPublicado(evento.id(), clock.instant());
		} catch (RuntimeException e) {
			logGateway.aviso(MENSAGEM_FALHA_PUBLICACAO, evento.id(), e);
		}
	}
}
