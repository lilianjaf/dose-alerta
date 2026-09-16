package com.dosealerta.mensageria.infra.controller;

import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import com.dosealerta.mensageria.core.domain.SolicitacaoLigacao;
import com.dosealerta.mensageria.core.gateway.LigacaoGateway;
import com.dosealerta.mensageria.core.gateway.MensageriaGateway;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ponto de entrada usado pelo modulo-notificacao para acionar o envio técnico via Twilio.
 * Não conhece etapa de escalonamento nem regra de negócio — apenas recebe o conteúdo já
 * decidido e o repassa ao gateway correspondente.
 */
@RestController
class EnvioController {

	private final MensageriaGateway mensageriaGateway;
	private final LigacaoGateway ligacaoGateway;

	EnvioController(MensageriaGateway mensageriaGateway, LigacaoGateway ligacaoGateway) {
		this.mensageriaGateway = mensageriaGateway;
		this.ligacaoGateway = ligacaoGateway;
	}

	@PostMapping("/mensagens/enviar")
	ResponseEntity<Void> enviarMensagem(@Valid @RequestBody EnviarMensagemRequest request) {
		mensageriaGateway.enviarMensagem(
				new ContatoWhatsApp(request.telefone()),
				new ConteudoMensagem.ComBotaoConfirmacao(request.texto(), request.textoBotao()));
		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}

	@PostMapping("/ligacoes/realizar")
	ResponseEntity<Void> realizarLigacao(@Valid @RequestBody RealizarLigacaoRequest request) {
		ligacaoGateway.ligarParaConfirmar(
				new ContatoWhatsApp(request.telefone()), new SolicitacaoLigacao(request.textoFalado()));
		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}
}
