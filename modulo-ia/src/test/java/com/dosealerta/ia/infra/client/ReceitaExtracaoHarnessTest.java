package com.dosealerta.ia.infra.client;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.infra.config.GeminiClientConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Tag("harness")
class ReceitaExtracaoHarnessTest {

	private static final Path DIRETORIO_FIXTURES = Path.of("src/test/resources/harness-receitas");
	private static final List<String> EXTENSOES_IMAGEM = List.of(".jpg", ".jpeg", ".png");

	@Test
	void deveExtrairCorretamenteTodasAsReceitasDoConjuntoDeTeste() throws IOException {
		List<Path> imagens = listarImagensComFixtureEsperada();
		assumeTrue(!imagens.isEmpty(), "Nenhuma fixture em " + DIRETORIO_FIXTURES + " (ver README.md do diretório)");
		String apiKey = System.getenv("GEMINI_API_KEY");
		assumeTrue(apiKey != null && !apiKey.isBlank(), "GEMINI_API_KEY não configurada, harness pulado");

		RestClient client = new GeminiClientConfig()
				.geminiRestClient("https://generativelanguage.googleapis.com", apiKey, 3000, 60000);
		GeminiExtratorReceitaGateway gateway =
				new GeminiExtratorReceitaGateway(client, "gemini-3.5-flash-lite", 0.1, 2048, 1000, new SimpleMeterRegistry());
		ObjectMapper objectMapper = new ObjectMapper();

		List<String> falhas = new ArrayList<>();
		for (Path imagem : imagens) {
			Path esperadoJson = trocarExtensaoPorJson(imagem);
			ReceitaExtraida esperado = objectMapper.readValue(esperadoJson.toFile(), ReceitaExtraida.class);
			ReceitaExtraida obtido = gateway.extrair(Files.readAllBytes(imagem));

			if (!esperado.equals(obtido)) {
				falhas.add("%s: esperado=%s obtido=%s".formatted(imagem.getFileName(), esperado, obtido));
			} else {
				System.out.println(imagem.getFileName() + ": OK");
			}
		}

		assertTrue(falhas.isEmpty(), "Fixtures reprovadas:\n" + String.join("\n", falhas));
	}

	private List<Path> listarImagensComFixtureEsperada() throws IOException {
		if (!Files.isDirectory(DIRETORIO_FIXTURES)) {
			return List.of();
		}
		try (Stream<Path> arquivos = Files.list(DIRETORIO_FIXTURES)) {
			return arquivos
					.filter(p -> EXTENSOES_IMAGEM.stream().anyMatch(ext -> p.toString().toLowerCase().endsWith(ext)))
					.filter(p -> Files.exists(trocarExtensaoPorJson(p)))
					.toList();
		}
	}

	private Path trocarExtensaoPorJson(Path imagem) {
		String nome = imagem.getFileName().toString();
		String semExtensao = nome.substring(0, nome.lastIndexOf('.'));
		return imagem.resolveSibling(semExtensao + ".json");
	}
}
