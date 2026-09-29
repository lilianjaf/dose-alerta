package com.dosealerta.mensageria.infra.twilio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.exception.EnvioMensagemFalhouException;
import com.dosealerta.mensageria.infra.messaging.MensagemFormatter;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.type.PhoneNumber;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import tools.jackson.databind.ObjectMapper;

class TwilioMensageriaAdapterTest extends TesteUnitarioBase {

	private final MensagemFormatter mensagemFormatter = new MensagemFormatter(new ObjectMapper());
	private final TwilioMensageriaAdapter adapter = new TwilioMensageriaAdapter(mensagemFormatter, "+5511888888888");

	@Test
	void deveEnviarMensagemDeTextoPeloWhatsapp() {
		var contato = new ContatoWhatsApp("+5511999999999");
		var conteudo = new ConteudoMensagem.Texto("Hora de tomar Losartana");

		try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
			MessageCreator creator = mock(MessageCreator.class);
			messageMock
					.when(() -> Message.creator(
							any(PhoneNumber.class), any(PhoneNumber.class), eq("Hora de tomar Losartana")))
					.thenReturn(creator);

			adapter.enviarMensagem(contato, conteudo);

			messageMock.verify(() -> Message.creator(
					eq(new PhoneNumber("whatsapp:+5511999999999")),
					eq(new PhoneNumber("whatsapp:+5511888888888")),
					eq("Hora de tomar Losartana")));
			verify(creator).create();
		}
	}

	@Test
	void deveEnviarTemplateComContentSidEVariaveis() {
		var contato = new ContatoWhatsApp("+5511999999999");
		var conteudo = new ConteudoMensagem.Template("HX123", Map.of("1", "Losartana"));

		try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
			MessageCreator creator = mock(MessageCreator.class);
			messageMock
					.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), eq("")))
					.thenReturn(creator);

			adapter.enviarMensagem(contato, conteudo);

			verify(creator).setContentSid("HX123");
			verify(creator).setContentVariables("{\"1\":\"Losartana\"}");
			verify(creator).create();
		}
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoTwilioFalha() {
		var contato = new ContatoWhatsApp("+5511999999999");
		var conteudo = new ConteudoMensagem.Texto("texto");

		try (MockedStatic<Message> messageMock = mockStatic(Message.class)) {
			MessageCreator creator = mock(MessageCreator.class);
			when(creator.create()).thenThrow(new ApiException("erro"));
			messageMock
					.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), eq("texto")))
					.thenReturn(creator);

			assertThrows(EnvioMensagemFalhouException.class, () -> adapter.enviarMensagem(contato, conteudo));
		}
	}
}
