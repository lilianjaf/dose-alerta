package com.dosealerta.susmock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class SusCadastroControllerIntegrationTest extends TesteIntegracaoBase {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deveEncontrarCadastroDeTelefoneFixo() throws Exception {
		mockMvc.perform(get("/sus/cadastros/{telefone}", "+5511999990001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.telefone").value("+5511999990001"))
				.andExpect(jsonPath("$.nomeCompleto").value("Ana Paula Souza"));
	}

	@Test
	void deveRetornar404ParaTelefoneNaoCadastrado() throws Exception {
		mockMvc.perform(get("/sus/cadastros/{telefone}", "+5511988887777")).andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"));
	}

	@Test
	void deveEncontrarCadastroDeNumeroDeInscricaoFixo() throws Exception {
		mockMvc.perform(get("/sus/inscricoes/{numero}", "700000000000001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.numeroInscricao").value("700000000000001"))
				.andExpect(jsonPath("$.nomeCompleto").value("Fernanda Torres Lima"));
	}

	@Test
	void deveRetornar404ParaNumeroDeInscricaoNaoCadastrado() throws Exception {
		mockMvc.perform(get("/sus/inscricoes/{numero}", "999999999999999")).andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith("application/problem+json"));
	}

	@Test
	void deveExporEndpointDeHealthCheck() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
	}
}
