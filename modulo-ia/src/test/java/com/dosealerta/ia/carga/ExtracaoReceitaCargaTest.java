package com.dosealerta.ia.carga;

import static com.dosealerta.ia.IaFixtures.umaExtracaoValida;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.ReceitaExtraidaFixtures;
import com.dosealerta.ia.TesteIntegracaoBase;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Tag("carga")
class ExtracaoReceitaCargaTest extends TesteIntegracaoBase {

	private static final int EXTRACOES = Integer.getInteger("carga.extracoes", 400);
	private static final int CONCORRENCIA = Integer.getInteger("carga.concorrencia", 32);
	private static final long LATENCIA_IA_MS = Long.getLong("carga.latencia-ia-ms", 100);
	private static final long P95_ORCAMENTO_MS = Long.getLong("carga.p95-orcamento-ms", 2_000);

	@MockitoBean
	private ExtratorReceitaGateway extratorReceitaGateway;

	@Autowired
	private ExtrairReceitaUseCase extrairReceitaUseCase;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void extracoesConcorrentesTerminamSemErroEComP95DentroDoOrcamento() throws Exception {
		when(extratorReceitaGateway.extrair(any())).thenAnswer(invocacao -> {
			Thread.sleep(LATENCIA_IA_MS);
			return ReceitaExtraidaFixtures.umMedicamento("Losartana", "50mg", 24, 30);
		});

		List<Long> latenciasMs = Collections.synchronizedList(new ArrayList<>());
		List<Callable<Void>> tarefas = new ArrayList<>();
		for (int i = 0; i < EXTRACOES; i++) {
			tarefas.add(() -> {
				long inicio = System.nanoTime();
				extrairReceitaUseCase.executar(umaExtracaoValida());
				latenciasMs.add((System.nanoTime() - inicio) / 1_000_000);
				return null;
			});
		}

		long inicioTotal = System.nanoTime();
		try (ExecutorService pool = Executors.newFixedThreadPool(CONCORRENCIA)) {
			for (Future<Void> resultado : pool.invokeAll(tarefas)) {
				resultado.get();
			}
		}
		long totalMs = (System.nanoTime() - inicioTotal) / 1_000_000;

		List<Long> ordenadas = latenciasMs.stream().sorted().toList();
		long p95 = ordenadas.get((int) Math.ceil(ordenadas.size() * 0.95) - 1);
		System.out.printf(
				"[carga] extracao: %d receitas, concorrencia %d, total %d ms, p50 %d ms, p95 %d ms%n",
				EXTRACOES, CONCORRENCIA, totalMs, ordenadas.get(ordenadas.size() / 2), p95);

		assertEquals(EXTRACOES, latenciasMs.size());
		assertEquals(EXTRACOES, jdbcTemplate.queryForObject("select count(*) from receita", Integer.class));
		assertTrue(p95 <= P95_ORCAMENTO_MS, () -> "p95 " + p95 + "ms acima do orcamento " + P95_ORCAMENTO_MS + "ms");
	}
}
