package com.dosealerta.mensageria.infra.gateway;

import com.dosealerta.mensageria.TesteUnitarioBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortasDeInfraTest extends TesteUnitarioBase {

	private static final String MENSAGEM = "mensagem {}";
	private static final String ARGUMENTO = "argumento";

	private Slf4jLogGateway logGateway;

	@BeforeEach
	void setUp() {
		logGateway = new Slf4jLogGateway();
	}

	@Test
	void deveAceitarAvisoEErroSemFalhar() {
		logGateway.aviso(MENSAGEM, ARGUMENTO);
		logGateway.erro(MENSAGEM, ARGUMENTO);
	}
}
