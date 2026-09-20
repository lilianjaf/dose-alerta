package com.dosealerta.contratos;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

class ContratoTest {

	static final List<String> CONTRATOS = List.of(
			"criar-alarme", "solicitar-envio", "enviar-mensagem", "realizar-ligacao", "resposta-paciente", "registrar-interacao");

	@ParameterizedTest
	@ValueSource(strings = {"criar-alarme", "solicitar-envio", "enviar-mensagem", "realizar-ligacao", "resposta-paciente", "registrar-interacao"})
	void exemploCanonicoRespeitaOProprioSchema(String nome) {
		assertTrue(Contrato.carregar(nome).violacoes(Contrato.carregar(nome).exemplo()).isEmpty());
	}

	@ParameterizedTest
	@ValueSource(strings = {"criar-alarme", "solicitar-envio", "enviar-mensagem", "realizar-ligacao", "resposta-paciente", "registrar-interacao"})
	void schemaRejeitaPayloadSemQualquerCampoObrigatorio(String nome) {
		Contrato contrato = Contrato.carregar(nome);
		for (String campo : contrato.camposObrigatorios()) {
			assertFalse(contrato.violacoes(contrato.semCampo(campo)).isEmpty(), nome + " aceitou payload sem " + campo);
		}
	}

	@Test
	void schemaRejeitaCampoDesconhecidoEValorForaDoFormato() {
		Contrato solicitarEnvio = Contrato.carregar("solicitar-envio");
		assertFalse(solicitarEnvio.violacoes(solicitarEnvio.exemplo().replace("LEMBRETE_INICIAL", "OUTRA")).isEmpty());
		assertFalse(solicitarEnvio.violacoes(solicitarEnvio.exemplo().replace("+5511999999999", "11999")).isEmpty());
		assertFalse(solicitarEnvio.violacoes(solicitarEnvio.exemplo().replace("{", "{\"extra\":1,")).isEmpty());
		Contrato criarAlarme = Contrato.carregar("criar-alarme");
		assertFalse(criarAlarme.violacoes(criarAlarme.exemplo().replace("2026-09-19T08:00:00Z", "amanha")).isEmpty());
	}

	@Test
	void contratoInexistenteFalhaAltoEmVezDeValidarNada() {
		assertThrows(IllegalArgumentException.class, () -> Contrato.carregar("nao-existe"));
	}
}
