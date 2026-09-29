package com.dosealerta.ia.infra.controller;

import static com.dosealerta.ia.IaFixtures.IMAGEM;
import static com.dosealerta.ia.IaFixtures.INSTANTE_FIXO;
import static com.dosealerta.ia.IaFixtures.PACIENTE_ID;
import static com.dosealerta.ia.IaFixtures.RECEITA_ID;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.ia.ReceitaExtraidaFixtures;
import com.dosealerta.ia.TesteIntegracaoBase;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class ReceitaControllerIntegrationTest extends TesteIntegracaoBase {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ExtratorReceitaGateway extratorReceitaGateway;

	@MockitoBean
	private SchedulerClientGateway schedulerClientGateway;

	@Test
	void deveExtrairPersistirEConfirmarUmaReceita() throws Exception {
		when(extratorReceitaGateway.extrair(any())).thenReturn(ReceitaExtraidaFixtures.umMedicamento("Losartana", "50mg", 24, 30));

		var imagem = umaImagem();

		String resposta = mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.receitas.length()").value(1))
				.andExpect(jsonPath("$.receitas[0].medicamento").value("Losartana"))
				.andExpect(jsonPath("$.receitas[0].status").value("AGUARDANDO_CONFIRMACAO"))
				.andExpect(jsonPath("$.naoProcessados").isEmpty())
				.andReturn()
				.getResponse()
				.getContentAsString();

		UUID id = UUID.fromString(objectMapper.readTree(resposta).get("receitas").get(0).get("id").asText());

		mockMvc.perform(get("/receitas/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(jsonPath("$.medicamento").value("Losartana"));

		String corpoConfirmacao =
				"{\"medicamento\":\"Losartana\",\"dose\":\"50mg\",\"frequenciaHoras\":24,\"duracaoDias\":30}";
		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.contentType(MediaType.APPLICATION_JSON)
						.content(corpoConfirmacao))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADA"));
	}

	@Test
	void deveConfirmarSemCorpoMantendoOsDadosExtraidos() throws Exception {
		UUID id = extrairReceita("Aerolin spray 100 mcg", "2 doses", 6, 30);

		mockMvc.perform(post("/receitas/{id}/confirmar", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADA"))
				.andExpect(jsonPath("$.medicamento").value("Aerolin spray 100 mcg"))
				.andExpect(jsonPath("$.dose").value("2 doses"))
				.andExpect(jsonPath("$.frequenciaHoras").value(6))
				.andExpect(jsonPath("$.duracaoDias").value(30));
	}

	@Test
	void deveCorrigirApenasOsCamposEnviadosNaConfirmacao() throws Exception {
		UUID id = extrairReceita("Losartana", "50mg", 24, 30);

		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"dose\":\"100mg\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.medicamento").value("Losartana"))
				.andExpect(jsonPath("$.dose").value("100mg"))
				.andExpect(jsonPath("$.frequenciaHoras").value(24))
				.andExpect(jsonPath("$.duracaoDias").value(30));
	}

	@Test
	void deveRetornar400QuandoCampoInformadoNaConfirmacaoEInvalido() throws Exception {
		UUID id = extrairReceita("Losartana", "50mg", 24, 30);

		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"frequenciaHoras\":169}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"medicamento\":\"  \"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveConfirmarPorTelefoneSemPrecisarDoIdNemDeToken() throws Exception {
		extrairReceita("Aerolin spray 100 mcg", "2 doses", 6, 30);

		mockMvc.perform(post("/receitas/confirmar-por-telefone")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"telefone\":\"+5511999999999\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADA"))
				.andExpect(jsonPath("$.medicamento").value("Aerolin spray 100 mcg"));
	}

	@Test
	void deveCorrigirCamposAoConfirmarPorTelefone() throws Exception {
		extrairReceita("Losartana", "50mg", 24, 30);

		mockMvc.perform(post("/receitas/confirmar-por-telefone")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"telefone\":\"+5511999999999\",\"dose\":\"100mg\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.dose").value("100mg"))
				.andExpect(jsonPath("$.frequenciaHoras").value(24));
	}

	@Test
	void deveRetornar404AoConfirmarPorTelefoneSemReceitaPendente() throws Exception {
		mockMvc.perform(post("/receitas/confirmar-por-telefone")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"telefone\":\"+5511988880000\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveExtrairSemTokenPorSerChamadoPeloModuloMensageria() throws Exception {
		when(extratorReceitaGateway.extrair(any()))
				.thenReturn(ReceitaExtraidaFixtures.umMedicamento("Losartana", "50mg", 24, 30));
		var imagem = umaImagem();

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "+5511977776666")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isCreated());
	}

	@Test
	void deveRetornar404ParaReceitaInexistente() throws Exception {
		mockMvc.perform(get("/receitas/{id}", RECEITA_ID).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRejeitarAcessoSemToken() throws Exception {
		mockMvc.perform(get("/receitas/{id}", RECEITA_ID)).andExpect(status().isUnauthorized());
	}

	@Test
	void deveRejeitarExtracaoSemImagem() throws Exception {
		var imagemVazia = umaImagemVazia();

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagemVazia)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRejeitarExtracaoComTelefoneInvalido() throws Exception {
		var imagem = umaImagem();

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "numero-invalido")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar422QuandoGuardrailReprovaAExtracao() throws Exception {
		when(extratorReceitaGateway.extrair(any())).thenReturn(ReceitaExtraidaFixtures.umMedicamento(null, "50mg", 24, 30));

		var imagem = umaImagem();

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void deveRetornar422ComOrientacaoQuandoNaoHaReceitaFormalComPrescritorERegistro() throws Exception {
		when(extratorReceitaGateway.extrair(any()))
				.thenReturn(ReceitaExtraidaFixtures.semRegistroProfissional());

		var imagem = umaImagem();

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.detail").value(containsString("devidamente indicados por um profissional")))
				.andExpect(jsonPath("$.motivo").value("registro profissional (CRM/CRO) não identificado"));
	}

	@Test
	void deveCriarUmaReceitaParaCadaMedicamentoMesmoSemDadosCompletos() throws Exception {
		when(extratorReceitaGateway.extrair(any()))
				.thenReturn(ReceitaExtraidaFixtures.comVariosMedicamentosIncluindoUmSemNome());

		var imagem = umaImagem();

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.receitas.length()").value(3))
				.andExpect(jsonPath("$.receitas[0].medicamento").value("Amoxicilina 500mg"))
				.andExpect(jsonPath("$.receitas[0].camposPendentes").isEmpty())
				.andExpect(jsonPath("$.receitas[1].medicamento").value("Celebra 200mg"))
				.andExpect(jsonPath("$.receitas[2].medicamento").value("Decadron 4mg"))
				.andExpect(jsonPath("$.receitas[2].camposPendentes[0]").value("dose"))
				.andExpect(jsonPath("$.receitas[2].camposPendentes[1]").value("frequenciaHoras"))
				.andExpect(jsonPath("$.receitas[2].camposPendentes[2]").value("duracaoDias"))
				.andExpect(jsonPath("$.naoProcessados.length()").value(1))
				.andExpect(jsonPath("$.naoProcessados[0].motivo").value("medicamento não identificado"));
	}

	@Test
	void deveDeixarADuracaoEmBrancoEExigirlaNaConfirmacao() throws Exception {
		UUID id = extrairReceita("Amoxicilina", "1 comprimido", 8, null);

		mockMvc.perform(get("/receitas/{id}", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(jsonPath("$.duracaoDias").doesNotExist());

		mockMvc.perform(post("/receitas/{id}/confirmar", id).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.detail").value(containsString("duracaoDias")));

		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"duracaoDias\":7}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADA"))
				.andExpect(jsonPath("$.duracaoDias").value(7));
	}

	@Test
	void naoDeveConfirmarReceitaIncompletaEListarOQueFalta() throws Exception {
		when(extratorReceitaGateway.extrair(any()))
				.thenReturn(ReceitaExtraidaFixtures.somenteDecadronSemFrequenciaEDuracao());
		var imagem = umaImagem();
		String resposta = mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		UUID id = UUID.fromString(objectMapper.readTree(resposta).get("receitas").get(0).get("id").asText());

		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"duracaoDias\":1}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.camposPendentes[0]").value("frequenciaHoras"));

		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"frequenciaHoras\":24,\"duracaoDias\":1}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMADA"))
				.andExpect(jsonPath("$.camposPendentes").isEmpty());
	}

	@Test
	void deveAceitarFrequenciaSemanalNaConfirmacao() throws Exception {
		UUID id = extrairReceita("Vitamina D", "1 comprimido", 168, 120);

		mockMvc.perform(post("/receitas/{id}/confirmar", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"frequenciaHoras\":84}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.frequenciaHoras").value(84));
	}

	@Test
	void deveExporEndpointDeHealthCheck() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void devePropagarOCorrelationIdRecebidoNoHeaderDeResposta() throws Exception {
		mockMvc.perform(get("/receitas/{id}", RECEITA_ID)
						.header("X-Correlation-Id", "teste-123")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(header().string("X-Correlation-Id", "teste-123"));
	}

	@Test
	void deveExtrairComDadosFixosSemChamarOGeminiQuandoUsaOEndpointMock() throws Exception {
		var imagem = umaImagem();

		mockMvc.perform(multipart("/receitas/extrair-mock")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.receitas.length()").value(2))
				.andExpect(jsonPath("$.receitas[0].medicamento").value("Losartana 50mg"))
				.andExpect(jsonPath("$.receitas[0].status").value("AGUARDANDO_CONFIRMACAO"))
				.andExpect(jsonPath("$.receitas[1].medicamento").value("Amoxicilina 500mg"))
				.andExpect(jsonPath("$.receitas[1].camposPendentes[0]").value("dose"));

	}

	@Test
	void deveExporMetricasNoFormatoPrometheus() throws Exception {
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("jvm_memory_used_bytes")));
	}

	private UUID extrairReceita(String medicamento, String dose, int frequenciaHoras, Integer duracaoDias)
			throws Exception {
		when(extratorReceitaGateway.extrair(any()))
				.thenReturn(ReceitaExtraidaFixtures.umMedicamento(medicamento, dose, frequenciaHoras, duracaoDias));

		var imagem = umaImagem();
		String resposta = mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", PACIENTE_ID.toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", INSTANTE_FIXO.toString()))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return UUID.fromString(objectMapper.readTree(resposta).get("receitas").get(0).get("id").asText());
	}

	private MockMultipartFile umaImagem() {
		return new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, IMAGEM);
	}

	private MockMultipartFile umaImagemVazia() {
		return new MockMultipartFile("imagem", "vazia.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);
	}
}
