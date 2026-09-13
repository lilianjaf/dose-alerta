package com.dosealerta.scheduler.infra.client;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.exception.NotificacaoIndisponivelException;
import com.dosealerta.scheduler.core.gateway.NotificacaoClientGateway;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class HttpNotificacaoClientGateway implements NotificacaoClientGateway {

	private final RestClient notificacaoRestClient;

	HttpNotificacaoClientGateway(RestClient notificacaoRestClient) {
		this.notificacaoRestClient = notificacaoRestClient;
	}

	@Override
	public void solicitarEnvio(Alarme alarme, EtapaEscalonamento etapa) {
		try {
			notificacaoRestClient
					.post()
					.uri("/notificacoes/solicitar-envio")
					.body(SolicitarEnvioRequest.de(alarme, etapa))
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientException e) {
			throw new NotificacaoIndisponivelException(
					"Falha ao solicitar envio ao modulo-notificacao para o alarme " + alarme.getId(), e);
		}
	}
}
