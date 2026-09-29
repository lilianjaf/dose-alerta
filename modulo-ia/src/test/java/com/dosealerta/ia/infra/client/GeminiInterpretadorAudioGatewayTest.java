package com.dosealerta.ia.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.dto.IntencaoAudio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GeminiInterpretadorAudioGatewayTest {

	private static final String URL = "http://gemini/v1beta/models/gemini-3.5-flash-lite:generateContent";
	private static final String URL_MODELO_2 = "http://gemini/v1beta/models/gemini-3.8-flash:generateContent";

	private MockRestServiceServer servidorMock;
	private GeminiInterpretadorAudioGateway gateway;
	private GeminiInterpretadorAudioGateway gatewayComFallback;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder =
				RestClient.builder().baseUrl("http://gemini").defaultHeader("x-goog-api-key", "chave-de-teste");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new GeminiInterpretadorAudioGateway(builder.build(), "gemini-3.5-flash-lite", 0.1, 2048, 1);
	}

	private MockRestServiceServer criarGatewayComFallback() {
		RestClient.Builder builder =
				RestClient.builder().baseUrl("http://gemini").defaultHeader("x-goog-api-key", "chave-de-teste");
		MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
		gatewayComFallback =
				new GeminiInterpretadorAudioGateway(builder.build(), "gemini-3.5-flash-lite,gemini-3.8-flash", 0.1, 2048, 1);
		return servidor;
	}

	@Test
	void deveMontarARequisicaoComOAudioEInterpretarAIntencaoDeTomarADose() {
		servidorMock
				.expect(requestTo(URL))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.contents[0].parts[0].inlineData.mimeType").value("audio/ogg"))
				.andExpect(jsonPath("$.contents[0].parts[0].inlineData.data").isNotEmpty())
				.andRespond(withSuccess(respostaComTexto("{\"intencao\":\"TOMEI\"}"), MediaType.APPLICATION_JSON));

		AudioInterpretadoOutput resultado = gateway.interpretar(audio(), "audio/ogg; codecs=opus");

		assertEquals(new AudioInterpretadoOutput(IntencaoAudio.TOMEI, null, null, null), resultado);
	}

	@Test
	void deveExtrairOsCamposDeCorrecaoQuandoAIntencaoForCorrecao() {
		servidorMock
				.expect(requestTo(URL))
				.andRespond(withSuccess(
						respostaComTexto("{\"intencao\":\"CORRECAO\",\"dose\":\"1 comprimido\",\"frequenciaHoras\":8,"
								+ "\"duracaoDias\":7}"),
						MediaType.APPLICATION_JSON));

		AudioInterpretadoOutput resultado = gateway.interpretar(audio(), "audio/ogg");

		assertEquals(new AudioInterpretadoOutput(IntencaoAudio.CORRECAO, "1 comprimido", 8, 7), resultado);
	}

	@Test
	void deveUsarOMimeTypePadraoQuandoOTipoDeConteudoNaoForInformado() {
		servidorMock
				.expect(requestTo(URL))
				.andExpect(jsonPath("$.contents[0].parts[0].inlineData.mimeType").value("audio/ogg"))
				.andRespond(withSuccess(respostaComTexto("{\"intencao\":\"CONFIRMAR\"}"), MediaType.APPLICATION_JSON));

		gateway.interpretar(audio(), null);

		servidorMock.verify();
	}

	@Test
	void deveDevolverNaoEntendidoQuandoOGeminiFalhaEmTodasAsTentativas() {
		servidorMock.expect(ExpectedCount.times(4), requestTo(URL)).andRespond(withServerError());

		AudioInterpretadoOutput resultado = gateway.interpretar(audio(), "audio/ogg");

		assertEquals(AudioInterpretadoOutput.naoEntendido(), resultado);
		servidorMock.verify();
	}

	@Test
	void deveTentarNovamenteQuandoOServidorEstaSobrecarregado() {
		servidorMock.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
		servidorMock.expect(requestTo(URL)).andRespond(withSuccess(respostaComTexto("{\"intencao\":\"TOMEI\"}"), MediaType.APPLICATION_JSON));

		AudioInterpretadoOutput resultado = gateway.interpretar(audio(), "audio/ogg");

		assertEquals(new AudioInterpretadoOutput(IntencaoAudio.TOMEI, null, null, null), resultado);
		servidorMock.verify();
	}

	@Test
	void naoDeveTentarNovamenteQuandoOErroNaoETransitorio() {
		servidorMock.expect(ExpectedCount.once(), requestTo(URL)).andRespond(withStatus(HttpStatus.FORBIDDEN));

		AudioInterpretadoOutput resultado = gateway.interpretar(audio(), "audio/ogg");

		assertEquals(AudioInterpretadoOutput.naoEntendido(), resultado);
		servidorMock.verify();
	}

	@Test
	void naoDeveTentarNovamenteQuandoACotaEExcedida() {
		servidorMock.expect(ExpectedCount.once(), requestTo(URL)).andRespond(withTooManyRequests());

		AudioInterpretadoOutput resultado = gateway.interpretar(audio(), "audio/ogg");

		assertEquals(AudioInterpretadoOutput.naoEntendido(), resultado);
		servidorMock.verify();
	}

	@Test
	void deveCairParaOProximoModeloQuandoOPrimeiroEsgotaAsTentativas() {
		MockRestServiceServer servidor = criarGatewayComFallback();

		servidor.expect(ExpectedCount.times(4), requestTo(URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
		servidor.expect(requestTo(URL_MODELO_2))
				.andRespond(withSuccess(respostaComTexto("{\"intencao\":\"CONFIRMAR\"}"), MediaType.APPLICATION_JSON));

		AudioInterpretadoOutput resultado = gatewayComFallback.interpretar(audio(), "audio/ogg");

		assertEquals(new AudioInterpretadoOutput(IntencaoAudio.CONFIRMAR, null, null, null), resultado);
		servidor.verify();
	}

	@Test
	void deveDevolverNaoEntendidoQuandoTodosOsModelosConfiguradosFalham() {
		MockRestServiceServer servidor = criarGatewayComFallback();

		servidor.expect(ExpectedCount.times(4), requestTo(URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
		servidor.expect(ExpectedCount.times(4), requestTo(URL_MODELO_2)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

		AudioInterpretadoOutput resultado = gatewayComFallback.interpretar(audio(), "audio/ogg");

		assertEquals(AudioInterpretadoOutput.naoEntendido(), resultado);
		servidor.verify();
	}

	@Test
	void deveDevolverNaoEntendidoQuandoARespostaNaoEUmJsonValido() {
		servidorMock.expect(requestTo(URL)).andRespond(withSuccess(respostaComTexto("isto nao e json"), MediaType.APPLICATION_JSON));

		assertEquals(AudioInterpretadoOutput.naoEntendido(), gateway.interpretar(audio(), "audio/ogg"));
	}

	@Test
	void deveDevolverNaoEntendidoQuandoNaoHaCandidatos() {
		servidorMock.expect(requestTo(URL)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

		AudioInterpretadoOutput resultado = gateway.interpretar(audio(), "audio/ogg");

		assertEquals(IntencaoAudio.NAO_ENTENDIDO, resultado.intencao());
		assertNull(resultado.dose());
	}

	private String respostaComTexto(String texto) {
		String escapado = texto.replace("\\", "\\\\").replace("\"", "\\\"");
		return "{\"candidates\":[{\"content\":{\"role\":\"model\",\"parts\":[{\"text\":\"%s\"}]}}]}".formatted(escapado);
	}

	private byte[] audio() {
		return new byte[] {0x4F, 0x67, 0x67, 0x53, 0, 0, 0};
	}
}
