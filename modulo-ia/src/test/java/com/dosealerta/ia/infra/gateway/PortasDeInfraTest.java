package com.dosealerta.ia.infra.gateway;

import static com.dosealerta.ia.IaFixtures.CORRELATION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.ia.TesteUnitarioBase;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.slf4j.MDC;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

class PortasDeInfraTest extends TesteUnitarioBase {

	private static final String MDC_KEY = "correlationId";
	private static final String MENSAGEM = "mensagem {}";
	private static final String ARGUMENTO = "argumento";
	private static final String RESULTADO = "resultado";

	@Mock
	private TransactionTemplate transactionTemplate;

	@Mock
	private TransactionStatus status;

	private MdcCorrelacaoGateway correlacaoGateway;
	private TransactionGatewayImpl transactionGateway;
	private Slf4jLogGateway logGateway;

	@BeforeEach
	void setUp() {
		correlacaoGateway = new MdcCorrelacaoGateway();
		transactionGateway = new TransactionGatewayImpl(transactionTemplate);
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
	@SuppressWarnings("unchecked")
	void deveExecutarAAcaoDentroDaTransacao() {
		when(transactionTemplate.execute(any(TransactionCallback.class)))
				.thenAnswer(inv -> ((TransactionCallback<?>) inv.getArgument(0)).doInTransaction(status));
		Supplier<String> acao = () -> RESULTADO;

		assertEquals(RESULTADO, transactionGateway.execute(acao));
		verify(transactionTemplate).execute(any(TransactionCallback.class));
	}

	@Test
	void deveAceitarAvisoEErroSemFalhar() {
		logGateway.aviso(MENSAGEM, ARGUMENTO);
		logGateway.erro(MENSAGEM, ARGUMENTO);
	}
}
