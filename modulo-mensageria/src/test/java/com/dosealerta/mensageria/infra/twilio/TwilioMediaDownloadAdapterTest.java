package com.dosealerta.mensageria.infra.twilio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TwilioMediaDownloadAdapterTest extends TesteUnitarioBase {

	private HttpServer servidor;
	private final AtomicReference<String> cabecalhoAutorizacaoRecebido = new AtomicReference<>();
	private TwilioMediaDownloadAdapter adapter;

	@BeforeEach
	void setUp() throws Exception {
		byte[] bytesDaImagem = {1, 2, 3, 4};
		servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
		servidor.createContext("/media/ME123", exchange -> {
			cabecalhoAutorizacaoRecebido.set(exchange.getRequestHeaders().getFirst("Authorization"));
			exchange.sendResponseHeaders(200, bytesDaImagem.length);
			exchange.getResponseBody().write(bytesDaImagem);
			exchange.close();
		});
		servidor.start();

		adapter = new TwilioMediaDownloadAdapter("AC-conta-de-teste", "token-de-teste");
	}

	@AfterEach
	void tearDown() {
		servidor.stop(0);
	}

	@Test
	void deveBaixarAImagemAutenticandoComBasicAuth() {
		URI mediaUrl = URI.create("http://localhost:" + servidor.getAddress().getPort() + "/media/ME123");

		byte[] resultado = adapter.baixar(mediaUrl);

		assertArrayEquals(new byte[] {1, 2, 3, 4}, resultado);
		String esperado =
				"Basic " + Base64.getEncoder().encodeToString("AC-conta-de-teste:token-de-teste".getBytes());
		assertEquals(esperado, cabecalhoAutorizacaoRecebido.get());
	}
}
