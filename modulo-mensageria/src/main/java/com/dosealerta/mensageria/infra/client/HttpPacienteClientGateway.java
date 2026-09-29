package com.dosealerta.mensageria.infra.client;

import com.dosealerta.mensageria.core.dto.IdentificarPacienteResultado;
import com.dosealerta.mensageria.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.mensageria.core.exception.PacienteIndisponivelException;
import com.dosealerta.mensageria.core.gateway.PacienteClientGateway;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class HttpPacienteClientGateway implements PacienteClientGateway {

	private final RestClient usuarioRestClient;

	HttpPacienteClientGateway(RestClient usuarioRestClient) {
		this.usuarioRestClient = usuarioRestClient;
	}

	@Override
	public IdentificarPacienteResultado identificar(String telefone) {
		try {
			IdentificarPacienteResponse resposta = usuarioRestClient
					.post()
					.uri("/pacientes/identificar")
					.body(new IdentificarPacienteRequest(telefone))
					.retrieve()
					.body(IdentificarPacienteResponse.class);
			return new IdentificarPacienteResultado(
					resposta.pacienteId(), resposta.nome(), resposta.cadastroCompleto(), resposta.recemCriado());
		} catch (RestClientException e) {
			throw new PacienteIndisponivelException(telefone, e);
		}
	}

	@Override
	public void completarCadastro(String telefone, String numeroInscricaoSus) {
		try {
			usuarioRestClient
					.post()
					.uri("/pacientes/completar-cadastro")
					.body(new CompletarCadastroRequest(telefone, numeroInscricaoSus))
					.retrieve()
					.toBodilessEntity();
		} catch (HttpClientErrorException.UnprocessableContent | HttpClientErrorException.BadRequest e) {

			throw new NumeroInscricaoSusNaoEncontradoException(numeroInscricaoSus);
		} catch (RestClientException e) {
			throw new PacienteIndisponivelException(telefone, e);
		}
	}
}
