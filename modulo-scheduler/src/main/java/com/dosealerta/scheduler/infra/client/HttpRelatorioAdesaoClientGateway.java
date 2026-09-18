package com.dosealerta.scheduler.infra.client;

import com.dosealerta.scheduler.core.domain.EventoInteracao;
import com.dosealerta.scheduler.core.exception.RelatorioAdesaoIndisponivelException;
import com.dosealerta.scheduler.core.gateway.RelatorioAdesaoClientGateway;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class HttpRelatorioAdesaoClientGateway implements RelatorioAdesaoClientGateway {

	private final RestClient relatorioAdesaoRestClient;

	HttpRelatorioAdesaoClientGateway(RestClient relatorioAdesaoRestClient) {
		this.relatorioAdesaoRestClient = relatorioAdesaoRestClient;
	}

	@Override
	public void registrarInteracao(EventoInteracao evento) {
		try {
			relatorioAdesaoRestClient
					.post()
					.uri("/interacoes")
					.body(new RegistrarInteracaoRequest(
							evento.id(),
							evento.alarmeId(),
							evento.pacienteId(),
							evento.medicamento(),
							evento.tipo(),
							evento.registradaEm()))
					.retrieve()
					.toBodilessEntity();
		} catch (RestClientException e) {
			throw new RelatorioAdesaoIndisponivelException(
					"Falha ao registrar a interação " + evento.id() + " no modulo-relatorio-adesao", e);
		}
	}
}
