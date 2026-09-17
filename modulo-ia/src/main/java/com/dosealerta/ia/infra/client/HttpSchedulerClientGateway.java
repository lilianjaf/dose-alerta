package com.dosealerta.ia.infra.client;

import com.dosealerta.ia.core.exception.SchedulerIndisponivelException;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class HttpSchedulerClientGateway implements SchedulerClientGateway {

	private final RestClient schedulerRestClient;

	HttpSchedulerClientGateway(RestClient schedulerRestClient) {
		this.schedulerRestClient = schedulerRestClient;
	}

	@Override
	public void criarAlarme(UUID pacienteId, String telefone, String medicamento, String dose, Instant horarioAlvo) {
		try {
			schedulerRestClient
					.post()
					.uri("/alarmes")
					.body(new CriarAlarmeRequest(pacienteId, telefone, medicamento, dose, horarioAlvo))
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientException e) {
			throw new SchedulerIndisponivelException(
					"Falha ao criar o alarme no modulo-scheduler para o paciente " + pacienteId, e);
		}
	}
}
