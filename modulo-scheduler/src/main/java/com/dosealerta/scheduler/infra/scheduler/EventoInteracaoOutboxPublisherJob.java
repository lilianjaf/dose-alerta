package com.dosealerta.scheduler.infra.scheduler;

import com.dosealerta.scheduler.core.usecase.PublicarEventosInteracaoPendentesUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class EventoInteracaoOutboxPublisherJob {

	private final PublicarEventosInteracaoPendentesUseCase publicarEventosInteracaoPendentesUseCase;

	EventoInteracaoOutboxPublisherJob(PublicarEventosInteracaoPendentesUseCase publicarEventosInteracaoPendentesUseCase) {
		this.publicarEventosInteracaoPendentesUseCase = publicarEventosInteracaoPendentesUseCase;
	}

	@Scheduled(fixedDelayString = "${scheduler.outbox.intervalo-ms}")
	void executar() {
		publicarEventosInteracaoPendentesUseCase.executar();
	}
}
