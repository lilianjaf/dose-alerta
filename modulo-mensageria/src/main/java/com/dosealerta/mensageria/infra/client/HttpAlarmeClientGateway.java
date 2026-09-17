package com.dosealerta.mensageria.infra.client;

import com.dosealerta.mensageria.core.exception.AlarmeIndisponivelException;
import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class HttpAlarmeClientGateway implements AlarmeClientGateway {

	private final RestClient schedulerRestClient;

	HttpAlarmeClientGateway(RestClient schedulerRestClient) {
		this.schedulerRestClient = schedulerRestClient;
	}

	@Override
	public void registrarConfirmacao(String telefone) {
		postar("/alarmes/confirmacoes", telefone);
	}

	@Override
	public void registrarLigacaoAtendida(String telefone) {
		postar("/alarmes/ligacoes/atendidas", telefone);
	}

	private void postar(String uri, String telefone) {
		try {
			schedulerRestClient.post().uri(uri).body(new TelefoneRequest(telefone)).retrieve().toBodilessEntity();
		} catch (RestClientException e) {
			throw new AlarmeIndisponivelException(telefone, e);
		}
	}
}
