package com.dosealerta.mensageria.infra.controller;

import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarRespostaMensagemUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarStatusLigacaoUseCase;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TwilioWebhookController {

	private static final Logger log = LoggerFactory.getLogger(TwilioWebhookController.class);

	private final ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase;
	private final ProcessarConfirmacaoLigacaoUseCase processarConfirmacaoLigacaoUseCase;
	private final ProcessarStatusLigacaoUseCase processarStatusLigacaoUseCase;

	TwilioWebhookController(
			ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase,
			ProcessarConfirmacaoLigacaoUseCase processarConfirmacaoLigacaoUseCase,
			ProcessarStatusLigacaoUseCase processarStatusLigacaoUseCase) {
		this.processarRespostaMensagemUseCase = processarRespostaMensagemUseCase;
		this.processarConfirmacaoLigacaoUseCase = processarConfirmacaoLigacaoUseCase;
		this.processarStatusLigacaoUseCase = processarStatusLigacaoUseCase;
	}

	@PostMapping("/webhooks/twilio/mensagens")
	void receberResposta(
			@RequestParam("From") String telefone,
			@RequestParam(value = "Body", required = false) String corpo,
			@RequestParam(value = "ButtonText", required = false) String textoBotao) {
		log.info("Resposta recebida de {}: body='{}' botao='{}'", telefone, corpo, textoBotao);
		processarRespostaMensagemUseCase.executar(telefone, corpo, textoBotao);
	}

	@PostMapping(value = "/webhooks/twilio/ligacoes/confirmacao", produces = MediaType.APPLICATION_XML_VALUE)
	String receberConfirmacaoLigacao(
			@RequestParam("From") String telefone,
			@RequestParam("CallSid") String callSid,
			@RequestParam(value = "Digits", required = false) String digitos) {
		boolean confirmado = processarConfirmacaoLigacaoUseCase.executar(telefone, digitos);
		log.info(
				"Confirmação de ligação recebida de {} (call {}): digito='{}' confirmado={}",
				telefone,
				callSid,
				digitos,
				confirmado);
		return gerarTwimlResposta(confirmado);
	}

	@PostMapping("/webhooks/twilio/ligacoes/status")
	void receberStatusLigacao(
			@RequestParam("CallSid") String callSid,
			@RequestParam("CallStatus") String status,
			@RequestParam(value = "To", required = false) String telefone) {
		log.info("Status da ligação {} para {}: {}", callSid, telefone, status);
		processarStatusLigacaoUseCase.executar(telefone, status);
	}

	private String gerarTwimlResposta(boolean confirmado) {
		try {
			String mensagem = confirmado ? "Confirmação registrada, obrigado." : "Não reconhecemos sua resposta.";
			VoiceResponse response =
					new VoiceResponse.Builder().say(new Say.Builder(mensagem).build()).build();
			return response.toXml();
		} catch (TwiMLException e) {
			throw new IllegalStateException("Falha ao gerar TwiML de resposta ao paciente", e);
		}
	}
}
