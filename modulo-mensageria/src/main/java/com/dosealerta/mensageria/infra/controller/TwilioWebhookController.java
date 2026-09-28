package com.dosealerta.mensageria.infra.controller;

import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;
import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarMensagemRecebidaUseCase;
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

	// O Twilio manda o telefone do WhatsApp como "whatsapp:+5511999999999": o prefixo não é parte do telefone
	// (não é usado em lugar nenhum do domínio) e precisa ser removido aqui, na borda, antes de seguir adiante.
	private static final String PREFIXO_WHATSAPP = "whatsapp:";

	private final ProcessarMensagemRecebidaUseCase processarMensagemRecebidaUseCase;
	private final ProcessarConfirmacaoLigacaoUseCase processarConfirmacaoLigacaoUseCase;
	private final ProcessarStatusLigacaoUseCase processarStatusLigacaoUseCase;

	TwilioWebhookController(
			ProcessarMensagemRecebidaUseCase processarMensagemRecebidaUseCase,
			ProcessarConfirmacaoLigacaoUseCase processarConfirmacaoLigacaoUseCase,
			ProcessarStatusLigacaoUseCase processarStatusLigacaoUseCase) {
		this.processarMensagemRecebidaUseCase = processarMensagemRecebidaUseCase;
		this.processarConfirmacaoLigacaoUseCase = processarConfirmacaoLigacaoUseCase;
		this.processarStatusLigacaoUseCase = processarStatusLigacaoUseCase;
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
		processarMensagemRecebidaUseCase.executar(
				new DadosMensagemRecebida(telefone, corpo, textoBotao, mediaUrl0, numMedia, mediaContentType0));
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
			VoiceResponse response =
					new VoiceResponse.Builder().say(new Say.Builder(mensagem).build()).build();
			return response.toXml();
		} catch (TwiMLException e) {
			throw new IllegalStateException("Falha ao gerar TwiML de resposta ao paciente", e);
		}
	}
}
