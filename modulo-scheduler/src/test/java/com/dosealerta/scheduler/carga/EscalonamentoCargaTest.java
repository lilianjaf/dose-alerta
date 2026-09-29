package com.dosealerta.scheduler.carga;

import static com.dosealerta.scheduler.SchedulerFixtures.DOSE;
import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.MEDICAMENTO;
import static com.dosealerta.scheduler.SchedulerFixtures.PACIENTE_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.TELEFONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.scheduler.TesteIntegracaoBase;
import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCase;
import java.time.Duration;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@Tag("carga")
@SpringBootTest(properties = "scheduler.escalonamento.intervalo-ms=3600000")
class EscalonamentoCargaTest extends TesteIntegracaoBase {

	private static final int ALARMES = Integer.getInteger("carga.alarmes", 2_000);
	private static final long ORCAMENTO_SEGUNDOS = Long.getLong("carga.orcamento-segundos", 120);

	@Autowired
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	@Autowired
	private EscalonarAlarmesUseCase escalonarAlarmesUseCase;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void umCicloDoJobEscalonaTodosOsAlarmesVencidosDentroDoOrcamento() {
		IntStream.range(0, ALARMES)
				.forEach(i -> alarmeRepositoryGateway.salvar(Alarme.criar(
						PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, INSTANTE_FIXO.minusSeconds(60), INSTANTE_FIXO)));

		long inicio = System.nanoTime();
		escalonarAlarmesUseCase.executar();
		Duration duracao = Duration.ofNanos(System.nanoTime() - inicio);

		System.out.printf(
				"[carga] escalonamento: %d alarmes em %d ms (%.0f alarmes/s)%n",
				ALARMES, duracao.toMillis(), ALARMES / Math.max(0.001, duracao.toMillis() / 1000.0));

		Integer escalonados = jdbcTemplate.queryForObject(
				"select count(*) from alarme where etapa_atual = ?", Integer.class, EtapaEscalonamento.LEMBRETE_INICIAL.name());
		assertEquals(ALARMES, escalonados, "todo alarme vencido deve avancar para o lembrete inicial");
		assertTrue(
				duracao.toSeconds() <= ORCAMENTO_SEGUNDOS,
				() -> "ciclo levou " + duracao.toSeconds() + "s, orcamento " + ORCAMENTO_SEGUNDOS + "s");
	}
}
