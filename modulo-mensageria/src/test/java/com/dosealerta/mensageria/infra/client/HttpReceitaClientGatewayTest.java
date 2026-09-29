package com.dosealerta.mensageria.infra.client;

import static com.dosealerta.mensageria.MensageriaFixtures.IMAGEM;
import static com.dosealerta.mensageria.MensageriaFixtures.INSTANTE_FIXO;
import static com.dosealerta.mensageria.MensageriaFixtures.PACIENTE_ID;
import static com.dosealerta.mensageria.MensageriaFixtures.TELEFONE;
import static com.dosealerta.mensageria.MensageriaFixtures.TIPO_IMAGEM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import com.dosealerta.mensageria.core.dto.IntencaoAudio;
import com.dosealerta.mensageria.core.dto.InterpretacaoAudioResultado;
import com.dosealerta.mensageria.core.dto.ReceitaExtraidaResultado;
import com.dosealerta.mensageria.core.exception.DadosReceitaIncompletosException;
import com.dosealerta.mensageria.core.exception.ReceitaPendenteNaoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpReceitaClientGatewayTest extends TesteUnitarioBase {

	private MockRestServiceServer servidorMock;
	private HttpReceitaClientGateway gateway;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("http://modulo-ia");
		servidorMock = MockRestServiceServer.bindTo(builder).build();
		gateway = new HttpReceitaClientGateway(builder.build());
	}

	@Test
	void deveExtrairEnviandoUmMultipartComAImagemEOsCamposDoPaciente() {
		servidorMock
				.expect(requestTo("http://modulo-ia/receitas/extrair"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
				.andRespond(withSuccess(
						"{\"receitas\":[{\"medicamento\":\"Losartana\",\"camposPendentes\":[]}],\"naoProcessados\":[]}",
						MediaType.APPLICATION_JSON));

		ReceitaExtraidaResultado resultado = gateway.extrair(
				PACIENTE_ID, TELEFONE, INSTANTE_FIXO, IMAGEM, TIPO_IMAGEM);

		assertEquals(1, resultado.receitas().size());
		assertEquals("Losartana", resultado.receitas().get(0).medicamento());
		assertTrue(resultado.receitas().get(0).completa());
	}

	@Test
	void deveConfirmarPorTelefoneEDevolverOMedicamento() {
		servidorMock
				.expect(requestTo("http://modulo-ia/receitas/confirmar-por-telefone"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(jsonPath("$.telefone").value("+5511999999999"))
				.andRespond(withSuccess("{\"medicamento\":\"Losartana\"}", MediaType.APPLICATION_JSON));

		String medicamento = gateway.confirmarPorTelefone("+5511999999999", CorrecaoReceita.vazia());

		assertEquals("Losartana", medicamento);
	}

	@Test
	void deveLancarReceitaPendenteNaoEncontradaQuando404() {
		servidorMock
				.expect(requestTo("http://modulo-ia/receitas/confirmar-por-telefone"))
				.andRespond(withStatus(HttpStatus.NOT_FOUND));

		assertThrows(
				ReceitaPendenteNaoEncontradaException.class,
				() -> gateway.confirmarPorTelefone("+5511999999999", CorrecaoReceita.vazia()));
	}

	@Test
	void deveLancarDadosIncompletosComOsCamposPendentesQuando422() {
		servidorMock
				.expect(requestTo("http://modulo-ia/receitas/confirmar-por-telefone"))
				.andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
						.contentType(MediaType.APPLICATION_JSON)
						.body("{\"mensagem\":\"faltou\",\"camposPendentes\":[\"dose\",\"duracaoDias\"]}"));

		DadosReceitaIncompletosException excecao = assertThrows(
				DadosReceitaIncompletosException.class,
				() -> gateway.confirmarPorTelefone("+5511999999999", CorrecaoReceita.vazia()));

		assertEquals(2, excecao.getCamposPendentes().size());
	}

	@Test
	void deveInterpretarAudioEnviandoUmMultipartComOAudio() {
		servidorMock
				.expect(requestTo("http://modulo-ia/audio/interpretar"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
				.andRespond(withSuccess("{\"intencao\":\"TOMEI\"}", MediaType.APPLICATION_JSON));

		InterpretacaoAudioResultado resultado = gateway.interpretarAudio(new byte[] {1, 2, 3}, "audio/ogg");

		assertEquals(new InterpretacaoAudioResultado(IntencaoAudio.TOMEI, null, null, null), resultado);
	}

	@Test
	void deveDevolverNaoEntendidoQuandoOModuloIaFalha() {
		servidorMock.expect(requestTo("http://modulo-ia/audio/interpretar")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

		InterpretacaoAudioResultado resultado = gateway.interpretarAudio(new byte[] {1, 2, 3}, "audio/ogg");

		assertEquals(InterpretacaoAudioResultado.naoEntendido(), resultado);
	}
}
