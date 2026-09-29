package com.dosealerta.relatorioadesao.infra.controller;

import static com.dosealerta.relatorioadesao.RelatorioFixtures.ALARME_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.INSTANTE_FIXO;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.PACIENTE_ID;
import static com.dosealerta.relatorioadesao.RelatorioFixtures.idDaInteracao;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.relatorioadesao.TesteIntegracaoBase;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class RelatorioAdesaoControllerIntegrationTest extends TesteIntegracaoBase {

	@Autowired
	private MockMvc mockMvc;

	private int sequencia;

	@BeforeEach
	void reiniciarSequencia() {
		sequencia = 0;
	}

	@Test
	void deveCalcularATaxaDeAdesaoAposReceberInteracoes() throws Exception {
		UUID pacienteId = PACIENTE_ID;
		registrarInteracao(pacienteId, "Losartana", "CONFIRMACAO");
		registrarInteracao(pacienteId, "Losartana", "NAO_CONFIRMACAO");
		registrarInteracao(pacienteId, "Losartana", "CONFIRMACAO");

		mockMvc.perform(get("/pacientes/{id}/adesao", pacienteId).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].medicamento").value("Losartana"))
				.andExpect(jsonPath("$[0].totalConfirmados").value(2))
				.andExpect(jsonPath("$[0].totalNaoConfirmados").value(1))
				.andExpect(jsonPath("$[0].taxaConfirmacao").value(2.0 / 3.0));
	}

	@Test
	void deveRejeitarAcessoSemToken() throws Exception {
		mockMvc.perform(get("/pacientes/{id}/adesao", PACIENTE_ID)).andExpect(status().isUnauthorized());
	}

	@Test
	void deveFiltrarPorPeriodoQuandoInicioEFimSaoInformados() throws Exception {
		UUID pacienteId = PACIENTE_ID;
		registrarInteracao(pacienteId, "Losartana", "CONFIRMACAO");

		mockMvc.perform(get("/pacientes/{id}/adesao", pacienteId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("inicio", INSTANTE_FIXO.minusSeconds(3600).toString())
						.param("fim", INSTANTE_FIXO.plusSeconds(3600).toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].totalConfirmados").value(1));
	}

	@Test
	void deveRetornarListaVaziaParaPacienteSemInteracoes() throws Exception {
		mockMvc.perform(get("/pacientes/{id}/adesao", PACIENTE_ID).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	private void registrarInteracao(UUID pacienteId, String medicamento, String tipo) throws Exception {
		String corpo =
				"""
				{
				  "id": "%s",
				  "alarmeId": "%s",
				  "pacienteId": "%s",
				  "medicamento": "%s",
				  "tipo": "%s",
				  "registradaEm": "%s"
				}
				"""
						.formatted(idDaInteracao(++sequencia), ALARME_ID, pacienteId, medicamento, tipo, INSTANTE_FIXO);

		mockMvc.perform(post("/interacoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
				.andExpect(status().isAccepted());
	}
}
