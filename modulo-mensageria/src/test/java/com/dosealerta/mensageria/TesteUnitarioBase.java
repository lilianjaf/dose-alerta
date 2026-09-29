package com.dosealerta.mensageria;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public abstract class TesteUnitarioBase {

	private AutoCloseable mocks;

	@BeforeEach
	protected void iniciarMocks() {
		mocks = MockitoAnnotations.openMocks(this);
	}

	@AfterEach
	protected void liberarMocks() throws Exception {
		mocks.close();
	}
}
