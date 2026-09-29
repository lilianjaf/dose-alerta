package com.dosealerta.scheduler.infra.gateway;

import static com.dosealerta.scheduler.SchedulerFixtures.CORRELATION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.dosealerta.scheduler.TesteUnitarioBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class PortasDeInfraTest extends TesteUnitarioBase {

	private static final String MDC_KEY = "correlationId";
	private static final String MENSAGEM = "mensagem {}";
	private static final String ARGUMENTO = "argumento";

	private MdcCorrelacaoGateway correlacaoGateway;
	private Slf4jLogGateway logGateway;

	@BeforeEach
	void setUp() {
		correlacaoGateway = new MdcCorrelacaoGateway();
		logGateway = new Slf4jLogGateway();
		MDC.clear();
	}

	@AfterEach
	void limparMdc() {
		MDC.clear();
	}

	@Test
	void deveDevolverACorrelacaoDoMdcQuandoPresente() {
		MDC.put(MDC_KEY, CORRELATION_ID);

		assertEquals(CORRELATION_ID, correlacaoGateway.atual());
	}

	@Test
	void deveGerarUmaCorrelacaoQuandoMdcVazio() {
		assertNotNull(correlacaoGateway.atual());
	}

	@Test
	void deveDefinirACorrelacaoDuranteAAcaoELimparDepois() {
		correlacaoGateway.executarCom(CORRELATION_ID, () -> assertEquals(CORRELATION_ID, MDC.get(MDC_KEY)));

		assertNull(MDC.get(MDC_KEY));
	}

	@Test
	void deveAceitarAvisoEErroSemFalhar() {
		logGateway.aviso(MENSAGEM, ARGUMENTO);
		logGateway.erro(MENSAGEM, ARGUMENTO);
	}
}
