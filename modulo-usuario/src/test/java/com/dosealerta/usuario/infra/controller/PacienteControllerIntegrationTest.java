package com.dosealerta.usuario.infra.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
class PacienteControllerIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CadastroSusGateway cadastroSusGateway;

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair chaves = keyPairGenerator.generateKeyPair();
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPublic().getEncoded()));
		registry.add(
				"security.jwt.private-key",
				() -> Base64.getEncoder().encodeToString(chaves.getPrivate().getEncoded()));
	}

	private static String telefoneUnico() {
		return "+5511" + String.format("%09d", Math.abs(System.nanoTime() % 1_000_000_000L));
	}

	@Test
	void deveCadastrarELogarPaciente() throws Exception {
		String telefone = telefoneUnico();
		var cadastro = new CadastrarPacienteInput("Maria da Silva", telefone, "senha1234");

		mockMvc.perform(post("/pacientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cadastro)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.telefone").value(telefone));

		var login = new AutenticarPacienteInput(telefone, "senha1234");
		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(login)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token", notNullValue()));
	}

	@Test
	void deveRejeitarCadastroDuplicado() throws Exception {
		String telefone = telefoneUnico();
		var cadastro = new CadastrarPacienteInput("Maria da Silva", telefone, "senha1234");

		mockMvc.perform(post("/pacientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cadastro)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/pacientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cadastro)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveRejeitarLoginComSenhaErrada() throws Exception {
		String telefone = telefoneUnico();
		var cadastro = new CadastrarPacienteInput("Maria da Silva", telefone, "senha1234");

		mockMvc.perform(post("/pacientes")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(cadastro)))
				.andExpect(status().isCreated());

		var login = new AutenticarPacienteInput(telefone, "senha-errada");
		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(login)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveRejeitarAcessoSemToken() throws Exception {
		mockMvc.perform(post("/rota-inexistente-protegida")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveExporEndpointDeHealthCheckSemAutenticar() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void deveGerarUmCorrelationIdQuandoAusente() throws Exception {
		mockMvc.perform(get("/actuator/health")).andExpect(header().exists("X-Correlation-Id"));
	}

	@Test
	void deveExporMetricasNoFormatoPrometheusSemAutenticar() throws Exception {
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("jvm_memory_used_bytes")));
	}

	@Test
	void deveIdentificarPacienteJaCadastradoSemChamarOSus() throws Exception {
		String telefone = telefoneUnico();
		mockMvc.perform(post("/pacientes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new CadastrarPacienteInput("Maria da Silva", telefone, "senha1234"))));

		mockMvc.perform(post("/pacientes/identificar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new IdentificarPacienteInput(telefone))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Maria da Silva"))
				.andExpect(jsonPath("$.cadastroCompleto").value(true));

		verifyNoInteractions(cadastroSusGateway);
	}

	@Test
	void deveIdentificarDireitoQuandoOSusAchaOTelefone() throws Exception {
		String telefone = telefoneUnico();
		when(cadastroSusGateway.buscarNomePorTelefone(telefone)).thenReturn(Optional.of("Joana Souza"));

		mockMvc.perform(post("/pacientes/identificar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new IdentificarPacienteInput(telefone))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Joana Souza"))
				.andExpect(jsonPath("$.cadastroCompleto").value(true));
	}

	@Test
	void deveCriarCadastroIncompletoQuandoOSusNaoAchaOTelefoneEDepoisCompletarComoNumeroDeInscricao() throws Exception {
		String telefone = telefoneUnico();
		String numeroInscricaoSus = "700000000000009";
		when(cadastroSusGateway.buscarNomePorTelefone(telefone)).thenReturn(Optional.empty());
		when(cadastroSusGateway.buscarNomePorNumeroInscricao(numeroInscricaoSus)).thenReturn(Optional.of("Pedro Alves"));

		mockMvc.perform(post("/pacientes/identificar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new IdentificarPacienteInput(telefone))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").doesNotExist())
				.andExpect(jsonPath("$.cadastroCompleto").value(false));

		mockMvc.perform(post("/pacientes/identificar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new IdentificarPacienteInput(telefone))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cadastroCompleto").value(false));
		verify(cadastroSusGateway, times(1)).buscarNomePorTelefone(telefone);

		mockMvc.perform(post("/pacientes/completar-cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new CompletarCadastroInput(telefone, numeroInscricaoSus))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Pedro Alves"));

		mockMvc.perform(post("/pacientes/identificar")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new IdentificarPacienteInput(telefone))))
				.andExpect(jsonPath("$.cadastroCompleto").value(true));
	}

	@Test
	void deveRetornar404AoCompletarCadastroDeTelefoneDesconhecido() throws Exception {
		mockMvc.perform(post("/pacientes/completar-cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new CompletarCadastroInput(telefoneUnico(), "700000000000000"))))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar422AoCompletarCadastroComNumeroDeInscricaoNaoEncontradoNoSus() throws Exception {
		String telefone = telefoneUnico();
		String numeroInscricaoSus = "999999999999999";
		when(cadastroSusGateway.buscarNomePorTelefone(telefone)).thenReturn(Optional.empty());
		when(cadastroSusGateway.buscarNomePorNumeroInscricao(numeroInscricaoSus)).thenReturn(Optional.empty());

		mockMvc.perform(post("/pacientes/identificar")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(new IdentificarPacienteInput(telefone))));

		mockMvc.perform(post("/pacientes/completar-cadastro")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new CompletarCadastroInput(telefone, numeroInscricaoSus))))
				.andExpect(status().isUnprocessableEntity());
	}
}
