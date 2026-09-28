package com.dosealerta.mensageria.infra.twilio;

import com.dosealerta.mensageria.core.gateway.MediaDownloadGateway;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Baixa a foto da receita que o paciente mandou no WhatsApp. A `MediaUrl` da Twilio já vem absoluta (não é
 * relativa a nenhum dos outros módulos), então este client não tem `baseUrl` fixo — diferente dos demais.
 */
@Component
class TwilioMediaDownloadAdapter implements MediaDownloadGateway {

	private final RestClient restClient;

	TwilioMediaDownloadAdapter(
			@Value("${twilio.account-sid}") String accountSid, @Value("${twilio.auth-token}") String authToken) {
		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(5000);
		requestFactory.setReadTimeout(10000);

		this.restClient = RestClient.builder()
				.requestFactory(requestFactory)
				.requestInterceptor((request, body, execution) -> {
					request.getHeaders().setBasicAuth(accountSid, authToken);
					return execution.execute(request, body);
				})
				.build();
	}

	@Override
	public byte[] baixar(URI mediaUrl) {
		return restClient.get().uri(mediaUrl).retrieve().body(byte[].class);
	}
}
