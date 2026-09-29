package com.dosealerta.mensageria.infra.controller;

import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;
import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarMensagemRecebidaUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarStatusLigacaoUseCase;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import com.twilio.twiml.voice.Say.Language;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TwilioWebhookController {

	private static final Logger log = LoggerFactory.getLogger(TwilioWebhookController.class);

	private static final String PREFIXO_WHATSAPP = "whatsapp:";

	private final ProcessarMensagemRecebidaUseCase processarMensagemRecebidaUseCase;
	private final ProcessarConfirmacaoLigacaoUseCase processarConfirmacaoLigacaoUseCase;
	private final ProcessarStatusLigacaoUseCase processarStatusLigacaoUseCase;
	private final Executor executorMensagemRecebida;

	TwilioWebhookController(
			ProcessarMensagemRecebidaUseCase processarMensagemRecebidaUseCase,
			ProcessarConfirmacaoLigacaoUseCase processarConfirmacaoLigacaoUseCase,
			ProcessarStatusLigacaoUseCase processarStatusLigacaoUseCase,
			Executor executorMensagemRecebida) {
		this.processarMensagemRecebidaUseCase = processarMensagemRecebidaUseCase;
		this.processarConfirmacaoLigacaoUseCase = processarConfirmacaoLigacaoUseCase;
		this.processarStatusLigacaoUseCase = processarStatusLigacaoUseCase;
		this.executorMensagemRecebida = executorMensagemRecebida;
	}

	@PostMapping("/webhooks/twilio/mensagens")
	void receberResposta(
			@RequestParam("From") String from,
			@RequestParam(value = "Body", required = false) String corpo,
			@RequestParam(value = "ButtonText", required = false) String textoBotao,
			@RequestParam(value = "MediaUrl0", required = false) String mediaUrl0,
			@RequestParam(value = "NumMedia", required = false, defaultValue = "0") int numMedia,
			@RequestParam(value = "MediaContentType0", required = false) String mediaContentType0) {
		String telefone = removerPrefixoWhatsapp(from);
		log.info(
				"Resposta recebida de {}: body='{}' botao='{}' numMedia={}", telefone, corpo, textoBotao, numMedia);
		var dados = new DadosMensagemRecebida(telefone, corpo, textoBotao, mediaUrl0, numMedia, mediaContentType0);
		executorMensagemRecebida.execute(() -> processarMensagemRecebidaUseCase.executar(dados));
	}

	private String removerPrefixoWhatsapp(String telefone) {
		return telefone.startsWith(PREFIXO_WHATSAPP) ? telefone.substring(PREFIXO_WHATSAPP.length()) : telefone;
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
			// Sem o idioma, o Twilio lê o texto em português com pronúncia em inglês por padrão — fica
			// incompreensível. Os outros dois <Say> da ligação (TwilioLigacaoAdapter) já acertam isso.
			VoiceResponse response = new VoiceResponse.Builder()
					.say(new Say.Builder(mensagem).language(Language.PT_BR).build())
					.build();
			return response.toXml();
		} catch (TwiMLException e) {
			throw new IllegalStateException("Falha ao gerar TwiML de resposta ao paciente", e);
		}
	}
}
