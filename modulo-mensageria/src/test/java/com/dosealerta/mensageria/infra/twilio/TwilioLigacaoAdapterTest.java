package com.dosealerta.mensageria.infra.twilio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.domain.SolicitacaoLigacao;
import com.dosealerta.mensageria.core.exception.LigacaoFalhouException;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.CallCreator;
import com.twilio.type.PhoneNumber;
import com.twilio.type.Twiml;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

class TwilioLigacaoAdapterTest extends TesteUnitarioBase {

	private final TwilioLigacaoAdapter adapter = new TwilioLigacaoAdapter("+5511777777777", "https://exemplo.com");

	@Test
	void deveLigarComTwimlDeConfirmacaoEStatusCallback() {
		var contato = new ContatoWhatsApp("+5511999999999");
		var solicitacao = new SolicitacaoLigacao("Hora de tomar sua medicação");

		try (MockedStatic<Call> callMock = mockStatic(Call.class)) {
			CallCreator creator = mock(CallCreator.class);
			when(creator.setStatusCallback(any(URI.class))).thenReturn(creator);
			when(creator.setStatusCallbackEvent(anyList())).thenReturn(creator);

			ArgumentCaptor<Twiml> twimlCaptor = ArgumentCaptor.forClass(Twiml.class);
			callMock
					.when(() -> Call.creator(any(PhoneNumber.class), any(PhoneNumber.class), twimlCaptor.capture()))
					.thenReturn(creator);

			adapter.ligarParaConfirmar(contato, solicitacao);

			callMock.verify(() -> Call.creator(
					eq(new PhoneNumber("+5511999999999")), eq(new PhoneNumber("+5511777777777")), any(Twiml.class)));
			verify(creator).setStatusCallback(URI.create("https://exemplo.com/webhooks/twilio/ligacoes/status"));
			verify(creator).create();

			String twiml = twimlCaptor.getValue().toString();
			assertTrue(twiml.contains("Hora de tomar sua medicação"));
			assertTrue(twiml.contains("https://exemplo.com/webhooks/twilio/ligacoes/confirmacao"));

			assertTrue(twiml.contains("language=\"pt-BR\""), twiml);
		}
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoTwilioFalha() {
		var contato = new ContatoWhatsApp("+5511999999999");
		var solicitacao = new SolicitacaoLigacao("Hora de tomar sua medicação");

		try (MockedStatic<Call> callMock = mockStatic(Call.class)) {
			CallCreator creator = mock(CallCreator.class);
			when(creator.setStatusCallback(any(URI.class))).thenReturn(creator);
			when(creator.setStatusCallbackEvent(anyList())).thenReturn(creator);
			when(creator.create()).thenThrow(new ApiException("erro"));
			callMock
					.when(() -> Call.creator(any(PhoneNumber.class), any(PhoneNumber.class), any(Twiml.class)))
					.thenReturn(creator);

			assertThrows(LigacaoFalhouException.class, () -> adapter.ligarParaConfirmar(contato, solicitacao));
		}
	}
}
