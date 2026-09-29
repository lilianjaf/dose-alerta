package com.dosealerta.ia.infra.controller;

import static com.dosealerta.ia.IaFixtures.RECEITA_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.ia.TesteIntegracaoBase;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ErrosHttpRealTest extends TesteIntegracaoBase {

	private static final String LIMITE = "----limite-teste";

	@LocalServerPort
	private int porta;

	@MockitoBean
	private ExtratorReceitaGateway extratorReceitaGateway;

	@MockitoBean
	private SchedulerClientGateway schedulerClientGateway;

	private final HttpClient http = HttpClient.newHttpClient();

	private HttpResponse<String> enviar(String caminho, String contentType, String corpo, String token) throws Exception {
		HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho))
				.header("Content-Type", contentType)
				.POST(HttpRequest.BodyPublishers.ofString(corpo));
		if (token != null) {
			requisicao.header("Authorization", "Bearer " + token);
		}
		return http.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
	}

	private static String multipartSemImagem() {
		return "--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"pacienteId\"\r\n\r\n" + RECEITA_ID
				+ "\r\n--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"telefone\"\r\n\r\n+5511999999999"
				+ "\r\n--" + LIMITE + "--\r\n";
	}

	@Test
	void deveRetornar400ComMensagemQuandoAImagemNaoEEnviada() throws Exception {
		HttpResponse<String> resposta = enviar(
				"/receitas/extrair", "multipart/form-data; boundary=" + LIMITE, multipartSemImagem(), tokenValido());

		assertEquals(400, resposta.statusCode());
		assertTrue(resposta.body().contains("imagem"), resposta.body());
	}

	@Test
	void deveRetornar400QuandoOCorpoDaConfirmacaoEMalformado() throws Exception {
		HttpResponse<String> resposta = enviar(
				"/receitas/" + RECEITA_ID + "/confirmar", "application/json", "{oops", tokenValido());

		assertEquals(400, resposta.statusCode());
	}

	@Test
	void deveRetornar400ComOCampoInvalidoNaConfirmacao() throws Exception {
		HttpResponse<String> resposta = enviar(
				"/receitas/" + RECEITA_ID + "/confirmar",
				"application/json",
				"{\"frequenciaHoras\":999}",
				tokenValido());

		assertEquals(400, resposta.statusCode());
		assertTrue(resposta.body().contains("frequenciaHoras"), resposta.body());
	}

	@Test
	void deveRetornar400ComMensagemQuandoUmParametroObrigatorioFalta() throws Exception {
		String corpo = "--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"imagem\"; filename=\"r.jpg\"\r\n"
				+ "Content-Type: image/jpeg\r\n\r\nabc\r\n--" + LIMITE
				+ "\r\nContent-Disposition: form-data; name=\"pacienteId\"\r\n\r\n" + RECEITA_ID
				+ "\r\n--" + LIMITE + "\r\nContent-Disposition: form-data; name=\"telefone\"\r\n\r\n+5511999999999"
				+ "\r\n--" + LIMITE + "--\r\n";

		HttpResponse<String> resposta =
				enviar("/receitas/extrair", "multipart/form-data; boundary=" + LIMITE, corpo, tokenValido());

		assertEquals(400, resposta.statusCode());
		assertTrue(resposta.body().contains("horarioInicial"), resposta.body());
	}

	@Test
	void deveRetornar415QuandoOExtrairRecebeJsonEmVezDeMultipart() throws Exception {
		HttpResponse<String> resposta = enviar("/receitas/extrair", "application/json", "{}", tokenValido());

		assertEquals(415, resposta.statusCode());
	}

	@Test
	void deveContinuarRetornando401SemTokenOuComTokenInvalidoNaRotaDoPacienteApp() throws Exception {

		assertEquals(401, enviarGet("/receitas/" + RECEITA_ID, null).statusCode());
		assertEquals(401, enviarGet("/receitas/" + RECEITA_ID, "token-invalido").statusCode());
	}

	private HttpResponse<String> enviarGet(String caminho, String token) throws Exception {
		HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho)).GET();
		if (token != null) {
			requisicao.header("Authorization", "Bearer " + token);
		}
		return http.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
	}
}
