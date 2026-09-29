package com.dosealerta.usuario.infra.controller;

import static com.dosealerta.usuario.UsuarioFixtures.NOME;
import static com.dosealerta.usuario.UsuarioFixtures.NOME_NO_SUS;
import static com.dosealerta.usuario.UsuarioFixtures.NUMERO_INSCRICAO_SUS;
import static com.dosealerta.usuario.UsuarioFixtures.SENHA_ERRADA;
import static com.dosealerta.usuario.UsuarioFixtures.TELEFONE;
import static com.dosealerta.usuario.UsuarioFixtures.umCadastroValido;
import static com.dosealerta.usuario.UsuarioFixtures.umCompletarCadastroComNumeroInscricao;
import static com.dosealerta.usuario.UsuarioFixtures.umCompletarCadastroValido;
import static com.dosealerta.usuario.UsuarioFixtures.umLoginComSenha;
import static com.dosealerta.usuario.UsuarioFixtures.umLoginValido;
import static com.dosealerta.usuario.UsuarioFixtures.umaIdentificacaoValida;
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

import com.dosealerta.usuario.TesteIntegracaoBase;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
class PacienteControllerIntegrationTest extends TesteIntegracaoBase {

	private static final String NUMERO_INSCRICAO_INEXISTENTE = "999999999999999";
	private static final String CAMINHO_PACIENTES = "/pacientes";
	private static final String CAMINHO_LOGIN = "/auth/login";
	private static final String CAMINHO_IDENTIFICAR = "/pacientes/identificar";
	private static final String CAMINHO_COMPLETAR = "/pacientes/completar-cadastro";
	private static final String CAMINHO_PROTEGIDO = "/rota-inexistente-protegida";
	private static final String HEALTH = "/actuator/health";
	private static final String PROMETHEUS = "/actuator/prometheus";
	private static final String PROBLEM_JSON = "application/problem+json";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private CadastroSusGateway cadastroSusGateway;

	private ResultActions postJson(String caminho, Object corpo) throws Exception {
		return mockMvc.perform(post(caminho)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(corpo)));
	}

	private void cadastrarPaciente() throws Exception {
		postJson(CAMINHO_PACIENTES, umCadastroValido()).andExpect(status().isCreated());
	}

	@Test
	void deveCadastrarELogarPaciente() throws Exception {
		postJson(CAMINHO_PACIENTES, umCadastroValido())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.telefone").value(TELEFONE));

		postJson(CAMINHO_LOGIN, umLoginValido())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token", notNullValue()));
	}

	@Test
	void deveRejeitarCadastroDuplicadoComProblemDetail() throws Exception {
		cadastrarPaciente();

		postJson(CAMINHO_PACIENTES, umCadastroValido())
				.andExpect(status().isConflict())
				.andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(409))
				.andExpect(jsonPath("$.detail", containsString(TELEFONE)));
	}

	@Test
	void deveRejeitarLoginComSenhaErrada() throws Exception {
		cadastrarPaciente();

		postJson(CAMINHO_LOGIN, umLoginComSenha(SENHA_ERRADA))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON));
	}

	@Test
	void deveRejeitarAcessoSemToken() throws Exception {
		mockMvc.perform(post(CAMINHO_PROTEGIDO).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveExporEndpointDeHealthCheckSemAutenticar() throws Exception {
		mockMvc.perform(get(HEALTH)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void deveGerarUmCorrelationIdQuandoAusente() throws Exception {
		mockMvc.perform(get(HEALTH)).andExpect(header().exists("X-Correlation-Id"));
	}

	@Test
	void deveExporMetricasNoFormatoPrometheusSemAutenticar() throws Exception {
		mockMvc.perform(get(PROMETHEUS))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("jvm_memory_used_bytes")));
	}

	@Test
	void deveIdentificarPacienteJaCadastradoSemChamarOSus() throws Exception {
		cadastrarPaciente();

		postJson(CAMINHO_IDENTIFICAR, umaIdentificacaoValida())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value(NOME))
				.andExpect(jsonPath("$.cadastroCompleto").value(true));

		verifyNoInteractions(cadastroSusGateway);
	}

	@Test
	void deveIdentificarDireitoQuandoOSusAchaOTelefone() throws Exception {
		when(cadastroSusGateway.buscarNomePorTelefone(TELEFONE)).thenReturn(Optional.of(NOME_NO_SUS));

		postJson(CAMINHO_IDENTIFICAR, umaIdentificacaoValida())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value(NOME_NO_SUS))
				.andExpect(jsonPath("$.cadastroCompleto").value(true));
	}

	@Test
	void deveCriarCadastroIncompletoQuandoOSusNaoAchaOTelefoneEDepoisCompletarComONumeroDeInscricao() throws Exception {
		when(cadastroSusGateway.buscarNomePorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(cadastroSusGateway.buscarNomePorNumeroInscricao(NUMERO_INSCRICAO_SUS))
				.thenReturn(Optional.of(NOME_NO_SUS));

		postJson(CAMINHO_IDENTIFICAR, umaIdentificacaoValida())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").doesNotExist())
				.andExpect(jsonPath("$.cadastroCompleto").value(false));

		postJson(CAMINHO_IDENTIFICAR, umaIdentificacaoValida())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.cadastroCompleto").value(false));
		verify(cadastroSusGateway, times(1)).buscarNomePorTelefone(TELEFONE);

		postJson(CAMINHO_COMPLETAR, umCompletarCadastroValido())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value(NOME_NO_SUS));

		postJson(CAMINHO_IDENTIFICAR, umaIdentificacaoValida())
				.andExpect(jsonPath("$.cadastroCompleto").value(true));
	}

	@Test
	void deveRetornar404AoCompletarCadastroDeTelefoneDesconhecido() throws Exception {
		postJson(CAMINHO_COMPLETAR, umCompletarCadastroValido())
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON));
	}

	@Test
	void deveRetornar422AoCompletarCadastroComNumeroDeInscricaoNaoEncontradoNoSus() throws Exception {
		when(cadastroSusGateway.buscarNomePorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(cadastroSusGateway.buscarNomePorNumeroInscricao(NUMERO_INSCRICAO_INEXISTENTE))
				.thenReturn(Optional.empty());
		postJson(CAMINHO_IDENTIFICAR, umaIdentificacaoValida());

		postJson(CAMINHO_COMPLETAR, umCompletarCadastroComNumeroInscricao(NUMERO_INSCRICAO_INEXISTENTE))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(content().contentTypeCompatibleWith(PROBLEM_JSON));
	}
}
