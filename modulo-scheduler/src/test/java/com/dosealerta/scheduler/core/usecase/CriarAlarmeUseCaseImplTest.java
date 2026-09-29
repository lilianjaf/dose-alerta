package com.dosealerta.scheduler.core.usecase;

import static com.dosealerta.scheduler.SchedulerFixtures.CLOCK_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.MEDICAMENTO;
import static com.dosealerta.scheduler.SchedulerFixtures.PACIENTE_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarme;
import static com.dosealerta.scheduler.SchedulerFixtures.umaCriacaoCom;
import static com.dosealerta.scheduler.SchedulerFixtures.umaCriacaoValida;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.SchedulerFixtures;
import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.dto.ResultadoCriarAlarme;
import com.dosealerta.scheduler.core.exception.MedicamentoObrigatorioException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.core.rules.criar.ValidadorCriacaoAlarmeRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class CriarAlarmeUseCaseImplTest extends TesteUnitarioBase {

	private static final String MEDICAMENTO_COM_ESPACOS = "  Losartana ";
	private static final String AEROLIN = "Aerolin spray 100 mcg";
	private static final String DOSE_AEROLIN = "2 doses";

	@Mock
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	@Mock
	private ValidadorCriacaoAlarmeRule regra;

	private CriarAlarmeUseCaseImpl useCase;
	private CriarAlarmeInput input;

	@BeforeEach
	void setUp() {
		useCase = new CriarAlarmeUseCaseImpl(alarmeRepositoryGateway, CLOCK_FIXO, List.of(regra));
		input = umaCriacaoValida();
	}

	@Test
	void deveCriarAlarmePendenteAPartirDoInputComDataDoRelogio() {
		when(alarmeRepositoryGateway.buscarPendentePorPacienteEMedicamento(PACIENTE_ID, MEDICAMENTO))
				.thenReturn(Optional.empty());
		when(alarmeRepositoryGateway.salvar(any(Alarme.class))).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriarAlarme resultado = useCase.executar(input);

		assertFalse(resultado.jaExistia());
		assertEquals(PACIENTE_ID, resultado.alarme().getPacienteId());
		assertEquals(input.dose(), resultado.alarme().getDose());
		assertEquals(INSTANTE_FIXO, resultado.alarme().getCriadoEm());
	}

	@Test
	void naoDeveDuplicarQuandoJaExisteAlarmePendenteDoMesmoMedicamento() {
		Alarme existente = umAlarme();
		when(alarmeRepositoryGateway.buscarPendentePorPacienteEMedicamento(PACIENTE_ID, AEROLIN))
				.thenReturn(Optional.of(existente));

		ResultadoCriarAlarme resultado = useCase.executar(
				umaCriacaoCom(PACIENTE_ID, SchedulerFixtures.TELEFONE, AEROLIN, DOSE_AEROLIN, INSTANTE_FIXO));

		assertTrue(resultado.jaExistia());
		assertSame(existente, resultado.alarme());
		verify(alarmeRepositoryGateway, never()).salvar(any(Alarme.class));
	}

	@Test
	void deveConsiderarOMedicamentoSemEspacosNasPontas() {
		when(alarmeRepositoryGateway.buscarPendentePorPacienteEMedicamento(PACIENTE_ID, MEDICAMENTO))
				.thenReturn(Optional.empty());
		when(alarmeRepositoryGateway.salvar(any(Alarme.class))).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriarAlarme resultado = useCase.executar(umaCriacaoCom(
				PACIENTE_ID, SchedulerFixtures.TELEFONE, MEDICAMENTO_COM_ESPACOS, SchedulerFixtures.DOSE, INSTANTE_FIXO));

		assertEquals(MEDICAMENTO, resultado.alarme().getMedicamento());
	}

	@Test
	void naoDeveConsultarNemSalvarQuandoAlgumaRegraFalha() {
		doThrow(new MedicamentoObrigatorioException()).when(regra).validar(any());

		assertThrows(MedicamentoObrigatorioException.class, () -> useCase.executar(input));

		verify(alarmeRepositoryGateway, never()).salvar(any());
		verify(alarmeRepositoryGateway, never()).buscarPendentePorPacienteEMedicamento(any(), any());
	}
}
