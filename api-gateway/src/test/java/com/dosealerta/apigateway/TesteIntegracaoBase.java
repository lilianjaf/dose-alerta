package com.dosealerta.apigateway;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.MDC;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Import(RelogioFixoTestConfig.class)
public abstract class TesteIntegracaoBase {

	@BeforeEach
	protected void limparMdcAntes() {
		MDC.clear();
	}

	@AfterEach
	protected void limparMdcDepois() {
		MDC.clear();
	}
}
