package com.dosealerta.mensageria.infra.twilio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.exception.EnvioMensagemFalhouException;
import com.twilio.exception.ApiException;
import org.junit.jupiter.api.Test;

class DetalheErroTwilioTest extends TesteUnitarioBase {

	@Test
	void deveDescreverStatusCodigoEMensagemDoErroDaTwilio() {
		var erro = new ApiException(
				"Too Many Requests", 20429, "https://www.twilio.com/docs/errors/20429", 429, 429, null, null, null);

		String descricao = DetalheErroTwilio.descrever(erro);

		assertTrue(descricao.contains("429"), descricao);
		assertTrue(descricao.contains("20429"), descricao);
		assertTrue(descricao.contains("Too Many Requests"), descricao);
	}

	@Test
	void deveEncontrarOErroDaTwilioMesmoEmbrulhadoEmOutraExcecao() {
		var api = new ApiException("Unable to create record", 21211, null, 400, 400, null, null, null);
		var embrulhado = new EnvioMensagemFalhouException("+5511999999999", api);

		assertTrue(DetalheErroTwilio.descrever(embrulhado).contains("21211"));
	}

	@Test
	void deveDescreverErrosQueNaoSaoDaTwilio() {
		assertEquals("IllegalStateException: falhou", DetalheErroTwilio.descrever(new IllegalStateException("falhou")));
	}

	@Test
	void deveMascararOTelefoneNosLogs() {
		assertEquals("****1234", DetalheErroTwilio.mascarar("+5511900001234"));
		assertEquals("****", DetalheErroTwilio.mascarar(null));
	}
}
