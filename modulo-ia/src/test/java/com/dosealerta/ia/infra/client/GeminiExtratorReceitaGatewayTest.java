package com.dosealerta.ia.infra.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

import com.dosealerta.ia.ReceitaExtraidaFixtures;
import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ExtracaoReceitaFalhouException;
import com.dosealerta.ia.core.exception.ImagemReceitaInvalidaException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GeminiExtratorReceitaGatewayTest {

	private static final String URL = "http://gemini/v1beta/models/gemini-3.5-flash-lite:generateContent";

	private static final String JSON_LOSARTANA = "{\"receitaMedica\":true,\"nomePrescritor\":\""
			+ ReceitaExtraidaFixtures.PRESCRITOR + "\",\"registroProfissional\":\"" + ReceitaExtraidaFixtures.REGISTRO
			+ "\",\"medicamentos\":[{\"medicamento\":\"Losartana\",\"dose\":\"50mg\",\"frequenciaHoras\":24,"
			+ "\"duracaoDias\":30}]}";

	private MockRestServiceServer servidorMock;
	private GeminiExtratorReceitaGateway gateway;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder =
				RestClient.builder().baseUrl("http://gemini").defaultHeader("x-goog-api-key", "chave-de-teste");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new GeminiExtratorReceitaGateway(builder.build(), "gemini-3.5-flash-lite", 0.1, 2048, 1, new SimpleMeterRegistry());
	}

	@Test
	void deveMapearARespostaEstruturadaParaOTipoDeDominio() {
		servidorMock
				.expect(requestTo(URL))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("x-goog-api-key", "chave-de-teste"))
				.andExpect(jsonPath("$.contents[0].role").value("user"))
				.andExpect(jsonPath("$.contents[0].parts[0].inlineData.mimeType").value("image/jpeg"))
				.andExpect(jsonPath("$.contents[0].parts[0].inlineData.data").isNotEmpty())
				.andExpect(jsonPath("$.systemInstruction.parts[0].text").isNotEmpty())
				.andExpect(jsonPath("$.generationConfig.responseMimeType").value("application/json"))
				.andExpect(jsonPath("$.generationConfig.responseSchema.type").value("OBJECT"))
				.andExpect(jsonPath("$.generationConfig.responseSchema.required[0]").value("receitaMedica"))
				.andExpect(jsonPath("$.generationConfig.responseSchema.properties.registroProfissional.nullable").value(true))
				.andExpect(jsonPath("$.generationConfig.responseSchema.properties.medicamentos.type").value("ARRAY"))
				.andExpect(jsonPath("$.generationConfig.responseSchema.properties.medicamentos.items.properties.duracaoDias.nullable").value(true))
				.andExpect(jsonPath("$.generationConfig.temperature").value(0.1))
				.andExpect(jsonPath("$.generationConfig.maxOutputTokens").value(2048))
				.andRespond(withSuccess(
						respostaComTexto(JSON_LOSARTANA, "STOP"),
						MediaType.APPLICATION_JSON));

		ReceitaExtraida resultado = gateway.extrair(imagemJpegMinima());

		assertEquals(ReceitaExtraidaFixtures.umMedicamento("Losartana", "50mg", 24, 30), resultado);
		servidorMock.verify();
	}

	@Test
	void deveInterpretarARespostaMesmoSemContentTypeNoHeader() {
		servidorMock
				.expect(requestTo(URL))
				.andRespond(withSuccess(
						respostaComTexto(JSON_LOSARTANA, "STOP"),
						null));

		assertEquals(ReceitaExtraidaFixtures.umMedicamento("Losartana", "50mg", 24, 30), gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveDevolverOResultadoQuandoAImagemNaoEUmaReceita() {
		servidorMock
				.expect(requestTo(URL))
				.andRespond(withSuccess(respostaComTexto("{\"receitaMedica\":false}", "STOP"), MediaType.APPLICATION_JSON));

		ReceitaExtraida resultado = gateway.extrair(imagemJpegMinima());

		assertFalse(resultado.receitaMedica());
	}

	@Test
	void deveExtrairTodosOsMedicamentosDeUmaReceitaDeDentista() {
		String json = "{\"receitaMedica\":true,\"nomePrescritor\":\"Dr. Exemplo da Silva\","
				+ "\"registroProfissional\":\"CRO/SC 99999\",\"medicamentos\":["
				+ "{\"medicamento\":\"Amoxicilina 500mg\",\"dose\":\"1 comprimido\",\"frequenciaHoras\":8,\"duracaoDias\":null},"
				+ "{\"medicamento\":\"Celebra 200mg\",\"dose\":\"1 cápsula\",\"frequenciaHoras\":12,\"duracaoDias\":5},"
				+ "{\"medicamento\":\"Decadron 4mg\",\"dose\":null,\"frequenciaHoras\":null,\"duracaoDias\":null}]}";
		servidorMock
				.expect(requestTo(URL))
				.andRespond(withSuccess(respostaComTexto(json, "STOP"), MediaType.APPLICATION_JSON));

		ReceitaExtraida resultado = gateway.extrair(imagemJpegMinima());

		assertEquals("Dr. Exemplo da Silva", resultado.nomePrescritor());
		assertEquals("CRO/SC 99999", resultado.registroProfissional());
		assertEquals(
				List.of(
						new MedicamentoExtraido("Amoxicilina 500mg", "1 comprimido", 8, null),
						new MedicamentoExtraido("Celebra 200mg", "1 cápsula", 12, 5),
						new MedicamentoExtraido("Decadron 4mg", null, null, null)),
				resultado.medicamentos());
	}

	@Test
	void deveLancarExcecaoQuandoARespostaNaoEJson() {
		servidorMock.expect(requestTo(URL)).andRespond(withSuccess("<html>erro</html>", MediaType.TEXT_HTML));

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveLancarExcecaoQuandoOPromptEBloqueado() {
		servidorMock
				.expect(requestTo(URL))
				.andRespond(withSuccess("{\"promptFeedback\":{\"blockReason\":\"SAFETY\"}}", MediaType.APPLICATION_JSON));

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveLancarExcecaoQuandoOModeloRecusaPorSeguranca() {
		servidorMock
				.expect(requestTo(URL))
				.andRespond(withSuccess(
						"{\"candidates\":[{\"finishReason\":\"SAFETY\"}]}", MediaType.APPLICATION_JSON));

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveLancarExcecaoQuandoARespostaFoiTruncada() {
		servidorMock
				.expect(requestTo(URL))
				.andRespond(withSuccess(respostaComTexto("{\"receitaMedica\":true,\"nomePr", "MAX_TOKENS"), MediaType.APPLICATION_JSON));

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveLancarExcecaoQuandoOTextoNaoEUmJsonValido() {
		servidorMock
				.expect(requestTo(URL))
				.andRespond(withSuccess(respostaComTexto("isto nao e json", "STOP"), MediaType.APPLICATION_JSON));

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveLancarExcecaoQuandoNaoHaCandidatos() {
		servidorMock.expect(requestTo(URL)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void naoDeveTentarNovamenteQuandoACotaEExcedida() {
		servidorMock.expect(ExpectedCount.once(), requestTo(URL)).andRespond(withTooManyRequests());

		ExtracaoReceitaFalhouException e =
				assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));

		assertEquals("Limite de uso do modelo de visão atingido. Tente novamente em alguns instantes", e.getMessage());
		servidorMock.verify();
	}

	@Test
	void deveTentarNovamenteQuandoOServidorEstaSobrecarregado() {
		servidorMock.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
		servidorMock
				.expect(requestTo(URL))
				.andRespond(withSuccess(
						respostaComTexto(JSON_LOSARTANA, "STOP"),
						MediaType.APPLICATION_JSON));

		ReceitaExtraida resultado = gateway.extrair(imagemJpegMinima());

		assertEquals(ReceitaExtraidaFixtures.umMedicamento("Losartana", "50mg", 24, 30), resultado);
		servidorMock.verify();
	}

	@Test
	void naoDeveTentarNovamenteQuandoOErroNaoETransitorio() {
		servidorMock.expect(ExpectedCount.once(), requestTo(URL)).andRespond(withStatus(HttpStatus.FORBIDDEN));

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
		servidorMock.verify();
	}

	@Test
	void deveLancarExcecaoDeDominioQuandoOServidorFalhaSemParar() {
		servidorMock.expect(ExpectedCount.times(4), requestTo(URL)).andRespond(withServerError());

		assertThrows(ExtracaoReceitaFalhouException.class, () -> gateway.extrair(imagemJpegMinima()));
	}

	@Test
	void deveRejeitarFormatoDeImagemNaoReconhecido() {
		assertThrows(ImagemReceitaInvalidaException.class, () -> gateway.extrair(imagemHeicMinima()));
	}

	// O texto do modelo vai como string dentro do JSON da resposta da API, então precisa de escape.
	private String respostaComTexto(String texto, String finishReason) {
		String escapado = texto.replace("\\", "\\\\").replace("\"", "\\\"");
		return "{\"candidates\":[{\"content\":{\"role\":\"model\",\"parts\":[{\"text\":\"%s\"}]},\"finishReason\":\"%s\"}]}"
				.formatted(escapado, finishReason);
	}

	private byte[] imagemJpegMinima() {
		return new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0};
	}

	private byte[] imagemHeicMinima() {
		return new byte[] {0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c', 0, 0, 0, 0};
	}
}
