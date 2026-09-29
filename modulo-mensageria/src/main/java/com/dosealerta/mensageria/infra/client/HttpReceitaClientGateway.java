package com.dosealerta.mensageria.infra.client;

import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import com.dosealerta.mensageria.core.dto.InterpretacaoAudioResultado;
import com.dosealerta.mensageria.core.dto.ReceitaCriada;
import com.dosealerta.mensageria.core.dto.ReceitaExtraidaResultado;
import com.dosealerta.mensageria.core.exception.DadosReceitaIncompletosException;
import com.dosealerta.mensageria.core.exception.ReceitaIndisponivelException;
import com.dosealerta.mensageria.core.exception.ReceitaPendenteNaoEncontradaException;
import com.dosealerta.mensageria.core.gateway.ReceitaClientGateway;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
class HttpReceitaClientGateway implements ReceitaClientGateway {

	private final RestClient iaRestClient;

	HttpReceitaClientGateway(RestClient iaRestClient) {
		this.iaRestClient = iaRestClient;
	}

	@Override
	public ReceitaExtraidaResultado extrair(
			UUID pacienteId, String telefone, Instant horarioInicial, byte[] imagem, String tipoConteudo) {
		MultipartBodyBuilder corpo = new MultipartBodyBuilder();
		corpo.part("imagem", new ByteArrayResource(imagem) {
					@Override
					public String getFilename() {
						return "receita";
					}
				})
				.contentType(mediaTypeOu(tipoConteudo));
		corpo.part("pacienteId", pacienteId.toString());
		corpo.part("telefone", telefone);
		corpo.part("horarioInicial", horarioInicial.toString());

		try {
			ExtracaoResponse resposta = iaRestClient
					.post()
					.uri("/receitas/extrair")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.body(corpo.build())
					.retrieve()
					.body(ExtracaoResponse.class);
			return converter(resposta);
		} catch (RestClientException e) {
			throw new ReceitaIndisponivelException(telefone, e);
		}
	}

	@Override
	public String confirmarPorTelefone(String telefone, CorrecaoReceita correcao) {
		try {
			ExtracaoResponse.ReceitaResponse resposta = iaRestClient
					.post()
					.uri("/receitas/confirmar-por-telefone")
					.body(new ConfirmarReceitaPorTelefoneRequest(
							telefone, correcao.medicamento(), correcao.dose(), correcao.frequenciaHoras(), correcao.duracaoDias()))
					.retrieve()
					.body(ExtracaoResponse.ReceitaResponse.class);
			return resposta.medicamento();
		} catch (HttpClientErrorException.NotFound e) {
			throw new ReceitaPendenteNaoEncontradaException(telefone);
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode().value() == 422) {
				DadosReceitaIncompletosResponse corpo = e.getResponseBodyAs(DadosReceitaIncompletosResponse.class);
				throw new DadosReceitaIncompletosException(corpo != null ? corpo.camposPendentes() : List.of());
			}
			throw new ReceitaIndisponivelException(telefone, e);
		} catch (RestClientException e) {
			throw new ReceitaIndisponivelException(telefone, e);
		}
	}

	@Override
	public InterpretacaoAudioResultado interpretarAudio(byte[] audio, String tipoConteudo) {
		MultipartBodyBuilder corpo = new MultipartBodyBuilder();
		corpo.part("audio", new ByteArrayResource(audio) {
					@Override
					public String getFilename() {
						return "audio";
					}
				})
				.contentType(mediaTypeOu(tipoConteudo));

		try {
			return iaRestClient
					.post()
					.uri("/audio/interpretar")
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.body(corpo.build())
					.retrieve()
					.body(InterpretacaoAudioResultado.class);
		} catch (RestClientException e) {
			return InterpretacaoAudioResultado.naoEntendido();
		}
	}

	private ReceitaExtraidaResultado converter(ExtracaoResponse resposta) {
		List<ReceitaCriada> receitas = resposta.receitas().stream()
				.map(r -> new ReceitaCriada(r.medicamento(), r.dose(), r.frequenciaHoras(), r.duracaoDias(), r.camposPendentes()))
				.toList();
		List<String> naoProcessados =
				resposta.naoProcessados().stream().map(ExtracaoResponse.MedicamentoNaoProcessadoResponse::medicamento).toList();
		return new ReceitaExtraidaResultado(receitas, naoProcessados);
	}

	private MediaType mediaTypeOu(String tipoConteudo) {
		return tipoConteudo != null ? MediaType.parseMediaType(tipoConteudo) : MediaType.APPLICATION_OCTET_STREAM;
	}
}
