package com.dosealerta.mensageria.infra.twilio;

import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.domain.SolicitacaoLigacao;
import com.dosealerta.mensageria.core.exception.LigacaoFalhouException;
import com.dosealerta.mensageria.core.gateway.LigacaoGateway;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.CallCreator;
import com.twilio.twiml.TwiMLException;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Say;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class TwilioLigacaoAdapter implements LigacaoGateway {

	private static final Logger LOG = LoggerFactory.getLogger(TwilioLigacaoAdapter.class);

	private static final String TEXTO_SEM_RESPOSTA = "Não recebemos sua confirmação. Até logo.";

	private final String numeroVoz;
	private final URI acaoConfirmacaoUrl;
	private final URI statusCallbackUrl;

	TwilioLigacaoAdapter(
			@Value("${twilio.voice-number}") String numeroVoz,
			@Value("${twilio.webhook-base-url}") String webhookBaseUrl) {
		this.numeroVoz = numeroVoz;
		String baseUrlSemBarraFinal =
				webhookBaseUrl.endsWith("/") ? webhookBaseUrl.substring(0, webhookBaseUrl.length() - 1) : webhookBaseUrl;
		this.acaoConfirmacaoUrl = URI.create(baseUrlSemBarraFinal + "/webhooks/twilio/ligacoes/confirmacao");
		this.statusCallbackUrl = URI.create(baseUrlSemBarraFinal + "/webhooks/twilio/ligacoes/status");
	}

	@Override
	public void ligarParaConfirmar(ContatoWhatsApp contato, SolicitacaoLigacao solicitacao) {
		PhoneNumber to = new PhoneNumber(contato.telefone());
		PhoneNumber from = new PhoneNumber(numeroVoz);
		Twiml twiml = new Twiml(gerarTwiml(solicitacao.textoFalado()));

		CallCreator creator = Call.creator(to, from, twiml)
				.setStatusCallback(statusCallbackUrl)
				.setStatusCallbackEvent(List.of("completed", "no-answer", "busy", "failed"));

		try {
			creator.create();
		} catch (TwilioException e) {
			LOG.warn("Twilio recusou o envio de ligação para {}: {}", DetalheErroTwilio.mascarar(contato.telefone()), DetalheErroTwilio.descrever(e));
			throw new LigacaoFalhouException(contato.telefone(), e);
		}
	}

	private String gerarTwiml(String textoFalado) {
		try {
			Gather gather = new Gather.Builder()
					.numDigits(1)
					.action(acaoConfirmacaoUrl)
					.say(new Say.Builder(textoFalado).build())
					.build();

			VoiceResponse response = new VoiceResponse.Builder()
					.gather(gather)
					.say(new Say.Builder(TEXTO_SEM_RESPOSTA).build())
					.build();

			return response.toXml();
		} catch (TwiMLException e) {
			throw new IllegalStateException("Falha ao gerar TwiML de confirmação da ligação", e);
		}
	}
}
