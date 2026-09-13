package com.dosealerta.scheduler.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CriarAlarmeUseCaseTest {

	@Mock
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	private CriarAlarmeUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new CriarAlarmeUseCase(alarmeRepositoryGateway);
	}

	@Test
	void deveCriarAlarmePendenteAPartirDoInput() {
		var input = new CriarAlarmeInput(UUID.randomUUID(), "Losartana", "50mg", Instant.now());
		when(alarmeRepositoryGateway.salvar(any(Alarme.class))).thenAnswer(inv -> inv.getArgument(0));

		Alarme resultado = useCase.executar(input);

		assertEquals(input.pacienteId(), resultado.getPacienteId());
		assertEquals(input.medicamento(), resultado.getMedicamento());
		assertEquals(input.dose(), resultado.getDose());
		verify(alarmeRepositoryGateway).salvar(any(Alarme.class));
	}
}
