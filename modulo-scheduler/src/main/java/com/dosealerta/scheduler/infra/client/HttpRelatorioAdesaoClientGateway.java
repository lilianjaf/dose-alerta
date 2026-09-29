package com.dosealerta.scheduler.infra.client;

import com.dosealerta.scheduler.core.domain.EventoInteracao;
import com.dosealerta.scheduler.core.exception.RelatorioAdesaoIndisponivelException;
import com.dosealerta.scheduler.core.gateway.RelatorioAdesaoClientGateway;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class HttpRelatorioAdesaoClientGateway implements RelatorioAdesaoClientGateway {

	private static final String MENSAGEM_FALHA_REGISTRAR_INTERACAO_PREFIXO = "Falha ao registrar a interação ";
	private static final String MENSAGEM_FALHA_REGISTRAR_INTERACAO_SUFIXO = " no modulo-relatorio-adesao";

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
					MENSAGEM_FALHA_REGISTRAR_INTERACAO_PREFIXO + evento.id() + MENSAGEM_FALHA_REGISTRAR_INTERACAO_SUFIXO, e);
		}
	}
}
