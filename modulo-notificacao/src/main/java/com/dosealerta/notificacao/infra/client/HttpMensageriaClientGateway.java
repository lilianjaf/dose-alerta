package com.dosealerta.notificacao.infra.client;

import com.dosealerta.notificacao.core.domain.OutboxEvent;
import com.dosealerta.notificacao.core.exception.MensageriaIndisponivelException;
import com.dosealerta.notificacao.core.gateway.MensageriaClientGateway;
import com.dosealerta.notificacao.core.rules.RegraConteudoNotificacao;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class HttpMensageriaClientGateway implements MensageriaClientGateway {

	private final RestClient mensageriaRestClient;

	HttpMensageriaClientGateway(RestClient mensageriaRestClient) {
		this.mensageriaRestClient = mensageriaRestClient;
	}

	@Override
	public void enviar(OutboxEvent evento) {
		try {
			switch (evento.canal()) {
				case MENSAGEM -> enviarMensagem(evento);
				case LIGACAO -> realizarLigacao(evento);
			}
		} catch (RestClientException e) {
			throw new MensageriaIndisponivelException(
					"Falha ao acionar o modulo-mensageria para o evento " + evento.id(), e);
		}
	}

	private void enviarMensagem(OutboxEvent evento) {
		String texto = RegraConteudoNotificacao.textoMensagem(evento.etapa(), evento.medicamento(), evento.dose());
		mensageriaRestClient
				.post()
				.uri("/mensagens/enviar")
				.body(new EnviarMensagemRequest(
						evento.telefone(), texto, RegraConteudoNotificacao.TEXTO_BOTAO_CONFIRMACAO))
				.retrieve()
				.toBodilessEntity();
	}

	private void realizarLigacao(OutboxEvent evento) {
		String textoFalado = RegraConteudoNotificacao.textoFalado(evento.medicamento(), evento.dose());
		mensageriaRestClient
				.post()
				.uri("/ligacoes/realizar")
				.body(new RealizarLigacaoRequest(evento.telefone(), textoFalado))
				.retrieve()
				.toBodilessEntity();
	}
}
