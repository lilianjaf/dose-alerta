package com.dosealerta.ia.infra.controller;

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
import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ReceitaControllerIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	private static RSAPrivateKey chavePrivada;

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		chavePrivada = (RSAPrivateKey) chaves.getPrivate();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
	}

	private static String tokenValido() throws Exception {
		Instant agora = Instant.now();
		JWTClaimsSet claims = new JWTClaimsSet.Builder()
				.subject("paciente-1")
				.issueTime(Date.from(agora))
				.expirationTime(Date.from(agora.plusSeconds(3600)))
				.build();
		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
		signedJWT.sign(new RSASSASigner(chavePrivada));
		return signedJWT.serialize();
	}

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

		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		String resposta = mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
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
		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511977776666")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isCreated());
	}

	@Test
	void deveRetornar404ParaReceitaInexistente() throws Exception {
		mockMvc.perform(get("/receitas/{id}", UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRejeitarAcessoSemToken() throws Exception {
		mockMvc.perform(get("/receitas/{id}", UUID.randomUUID())).andExpect(status().isUnauthorized());
	}

	@Test
	void deveRejeitarExtracaoSemImagem() throws Exception {
		var imagemVazia = new MockMultipartFile("imagem", "vazia.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]);

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagemVazia)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRejeitarExtracaoComTelefoneInvalido() throws Exception {
		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "numero-invalido")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar422QuandoGuardrailReprovaAExtracao() throws Exception {
		when(extratorReceitaGateway.extrair(any())).thenReturn(ReceitaExtraidaFixtures.umMedicamento(null, "50mg", 24, 30));

		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void deveRetornar422ComOrientacaoQuandoNaoHaReceitaFormalComPrescritorERegistro() throws Exception {
		when(extratorReceitaGateway.extrair(any()))
				.thenReturn(new ReceitaExtraida(
						true,
						"Dra. Exemplo",
						null,
						List.of(new MedicamentoExtraido("Losartana", "50mg", 24, 30))));

		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.mensagem").value(containsString("devidamente indicados por um profissional")))
				.andExpect(jsonPath("$.motivo").value("registro profissional (CRM/CRO) não identificado"));
	}

	@Test
	void deveCriarUmaReceitaParaCadaMedicamentoMesmoSemDadosCompletos() throws Exception {
		when(extratorReceitaGateway.extrair(any()))
				.thenReturn(ReceitaExtraidaFixtures.comMedicamentos(
						new MedicamentoExtraido("Amoxicilina 500mg", "1 comprimido", 8, 7),
						new MedicamentoExtraido("Celebra 200mg", "1 cápsula", 12, 5),
						new MedicamentoExtraido("Decadron 4mg", null, null, null),
						new MedicamentoExtraido(" ", "1 comprimido", 8, 7)));

		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
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
				.andExpect(jsonPath("$.mensagem").value(containsString("duracaoDias")));

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
				.thenReturn(ReceitaExtraidaFixtures.comMedicamentos(
						new MedicamentoExtraido("Decadron 4mg", "2 comprimidos", null, null)));
		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});
		String resposta = mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
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
		mockMvc.perform(get("/receitas/{id}", UUID.randomUUID())
						.header("X-Correlation-Id", "teste-123")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido()))
				.andExpect(header().string("X-Correlation-Id", "teste-123"));
	}

	@Test
	void deveExtrairComDadosFixosSemChamarOGeminiQuandoUsaOEndpointMock() throws Exception {
		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/receitas/extrair-mock")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
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

		var imagem = new MockMultipartFile("imagem", "receita.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[] {1, 2, 3});
		String resposta = mockMvc.perform(multipart("/receitas/extrair")
						.file(imagem)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenValido())
						.param("pacienteId", UUID.randomUUID().toString())
						.param("telefone", "+5511999999999")
						.param("horarioInicial", Instant.now().toString()))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return UUID.fromString(objectMapper.readTree(resposta).get("receitas").get(0).get("id").asText());
	}
}
