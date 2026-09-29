package com.dosealerta.scheduler.infra.scheduler;

import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class EscalonamentoJob {

	private final EscalonarAlarmesUseCase escalonarAlarmesUseCase;

	EscalonamentoJob(EscalonarAlarmesUseCase escalonarAlarmesUseCase) {
		this.escalonarAlarmesUseCase = escalonarAlarmesUseCase;
	}

	@Scheduled(fixedDelayString = "${scheduler.escalonamento.intervalo-ms}")
	void executar() {
		escalonarAlarmesUseCase.executar();
	}
}
