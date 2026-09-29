package com.dosealerta.usuario.infra.client;

import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class HttpCadastroSusGateway implements CadastroSusGateway {

	private static final String LOG_FALHA_CONSULTA_TELEFONE = "Falha ao consultar o cadastro SUS, seguindo para autocadastro";
	private static final String LOG_FALHA_CONSULTA_INSCRICAO = "Falha ao consultar a inscrição SUS informada pelo paciente";
	private static final Logger LOG = LoggerFactory.getLogger(HttpCadastroSusGateway.class);

	private final RestClient susMockRestClient;

	HttpCadastroSusGateway(RestClient susMockRestClient) {
		this.susMockRestClient = susMockRestClient;
	}

	@Override
	public Optional<String> buscarNomePorTelefone(String telefone) {
		try {
			CadastroSusOutput cadastro = susMockRestClient
					.get()
					.uri("/sus/cadastros/{telefone}", telefone)
					.retrieve()
					.body(CadastroSusOutput.class);
			return Optional.ofNullable(cadastro).map(CadastroSusOutput::nomeCompleto);
		} catch (HttpClientErrorException.NotFound e) {
			return Optional.empty();
		} catch (RestClientException e) {

			LOG.warn(LOG_FALHA_CONSULTA_TELEFONE, e);
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> buscarNomePorNumeroInscricao(String numeroInscricaoSus) {
		try {
			InscricaoSusOutput inscricao = susMockRestClient
					.get()
					.uri("/sus/inscricoes/{numero}", numeroInscricaoSus)
					.retrieve()
					.body(InscricaoSusOutput.class);
			return Optional.ofNullable(inscricao).map(InscricaoSusOutput::nomeCompleto);
		} catch (HttpClientErrorException.NotFound e) {
			return Optional.empty();
		} catch (RestClientException e) {
			LOG.warn(LOG_FALHA_CONSULTA_INSCRICAO, e);
			return Optional.empty();
		}
	}
}
