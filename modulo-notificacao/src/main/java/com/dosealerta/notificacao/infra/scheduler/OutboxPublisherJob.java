package com.dosealerta.notificacao.infra.scheduler;

import com.dosealerta.notificacao.core.usecase.PublicarEventosPendentesUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class OutboxPublisherJob {

	private final PublicarEventosPendentesUseCase publicarEventosPendentesUseCase;

	OutboxPublisherJob(PublicarEventosPendentesUseCase publicarEventosPendentesUseCase) {
		this.publicarEventosPendentesUseCase = publicarEventosPendentesUseCase;
	}

	@Scheduled(fixedDelayString = "${scheduler.outbox.intervalo-ms}")
	void executar() {
		publicarEventosPendentesUseCase.executar();
	}
}
