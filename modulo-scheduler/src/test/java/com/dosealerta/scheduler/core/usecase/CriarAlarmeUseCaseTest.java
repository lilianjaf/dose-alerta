package com.dosealerta.scheduler.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.dto.ResultadoCriarAlarme;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import java.time.Instant;
import java.util.Optional;
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
		var input = new CriarAlarmeInput(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now());
		when(alarmeRepositoryGateway.salvar(any(Alarme.class))).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriarAlarme resultado = useCase.executar(input);

		assertFalse(resultado.jaExistia());
		assertEquals(input.pacienteId(), resultado.alarme().getPacienteId());
		assertEquals(input.medicamento(), resultado.alarme().getMedicamento());
		assertEquals(input.dose(), resultado.alarme().getDose());
		verify(alarmeRepositoryGateway).salvar(any(Alarme.class));
	}

	@Test
	void naoDeveDuplicarQuandoJaExisteAlarmePendenteDoMesmoMedicamento() {
		UUID pacienteId = UUID.randomUUID();
		Alarme existente = Alarme.criar(pacienteId, "+5511999999999", "Aerolin spray 100 mcg", "2 doses", Instant.now());
		when(alarmeRepositoryGateway.buscarPendentePorPacienteEMedicamento(pacienteId, "Aerolin spray 100 mcg"))
				.thenReturn(Optional.of(existente));

		var input = new CriarAlarmeInput(
				pacienteId, "+5511999999999", "Aerolin spray 100 mcg", "2 doses", Instant.now().plusSeconds(3600));
		ResultadoCriarAlarme resultado = useCase.executar(input);

		assertTrue(resultado.jaExistia());
		assertSame(existente, resultado.alarme());
		verify(alarmeRepositoryGateway, never()).salvar(any(Alarme.class));
	}

	@Test
	void deveConsiderarOMedicamentoSemEspacosNasPontasNaBuscaEmNaCriacao() {
		UUID pacienteId = UUID.randomUUID();
		when(alarmeRepositoryGateway.buscarPendentePorPacienteEMedicamento(pacienteId, "Losartana"))
				.thenReturn(Optional.empty());
		when(alarmeRepositoryGateway.salvar(any(Alarme.class))).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriarAlarme resultado = useCase.executar(
				new CriarAlarmeInput(pacienteId, "+5511999999999", "  Losartana ", "50mg", Instant.now()));

		assertFalse(resultado.jaExistia());
		assertEquals("Losartana", resultado.alarme().getMedicamento());
	}
}
