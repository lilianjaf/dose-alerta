package com.dosealerta.susmock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
abstract class TesteIntegracaoBase {

	@BeforeEach
	protected void limparMdcAntes() {
		MDC.clear();
	}

	@AfterEach
	protected void limparMdcDepois() {
		MDC.clear();
	}
}
