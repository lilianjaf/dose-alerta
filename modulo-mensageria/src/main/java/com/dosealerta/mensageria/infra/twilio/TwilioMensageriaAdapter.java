package com.dosealerta.mensageria.infra.twilio;

import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.exception.EnvioMensagemFalhouException;
import com.dosealerta.mensageria.core.gateway.MensageriaGateway;
import com.dosealerta.mensageria.infra.messaging.MensagemFormatada;
import com.dosealerta.mensageria.infra.messaging.MensagemFormatter;
import com.twilio.exception.TwilioException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class TwilioMensageriaAdapter implements MensageriaGateway {

	private static final String LOG_MENSAGEM_RECUSADA = "Twilio recusou o envio de mensagem para {}: {}";
	private static final Logger LOG = LoggerFactory.getLogger(TwilioMensageriaAdapter.class);

	private static final String PREFIXO_WHATSAPP = "whatsapp:";

	private final MensagemFormatter mensagemFormatter;
	private final String numeroWhatsApp;

	TwilioMensageriaAdapter(
			MensagemFormatter mensagemFormatter, @Value("${twilio.whatsapp-number}") String numeroWhatsApp) {
		this.mensagemFormatter = mensagemFormatter;
		this.numeroWhatsApp = numeroWhatsApp;
	}

	@Override
	public void enviarMensagem(ContatoWhatsApp contato, ConteudoMensagem conteudo) {
		MensagemFormatada formatada = mensagemFormatter.formatar(conteudo);
		PhoneNumber to = new PhoneNumber(PREFIXO_WHATSAPP + contato.telefone());
		PhoneNumber from = new PhoneNumber(PREFIXO_WHATSAPP + numeroWhatsApp);

		MessageCreator creator = Message.creator(to, from, formatada.corpo());
		if (formatada.isTemplate()) {
			creator.setContentSid(formatada.contentSid());
			creator.setContentVariables(formatada.contentVariablesJson());
		}

		try {
			creator.create();
		} catch (TwilioException e) {
			LOG.warn(LOG_MENSAGEM_RECUSADA, DetalheErroTwilio.mascarar(contato.telefone()), DetalheErroTwilio.descrever(e));
			throw new EnvioMensagemFalhouException(contato.telefone(), e);
		}
	}
}
