package com.dosealerta.scheduler.core.rules.criar;

import static com.dosealerta.scheduler.SchedulerFixtures.DOSE;
import static com.dosealerta.scheduler.SchedulerFixtures.INSTANTE_FIXO;
import static com.dosealerta.scheduler.SchedulerFixtures.MEDICAMENTO;
import static com.dosealerta.scheduler.SchedulerFixtures.PACIENTE_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.TELEFONE;
import static com.dosealerta.scheduler.SchedulerFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.scheduler.SchedulerFixtures.umaCriacaoCom;
import static com.dosealerta.scheduler.SchedulerFixtures.umaCriacaoValida;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.exception.DoseObrigatoriaException;
import com.dosealerta.scheduler.core.exception.HorarioAlvoObrigatorioException;
import com.dosealerta.scheduler.core.exception.MedicamentoObrigatorioException;
import com.dosealerta.scheduler.core.exception.PacienteIdObrigatorioException;
import com.dosealerta.scheduler.core.exception.TelefoneObrigatorioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CriarRulesTest extends TesteUnitarioBase {

	private CriarPacienteIdDeveSerInformadoRule pacienteRule;
	private CriarTelefoneDevePreenchidoRule telefoneRule;
	private CriarMedicamentoDevePreenchidoRule medicamentoRule;
	private CriarDoseDevePreenchidaRule doseRule;
	private CriarHorarioAlvoDeveSerInformadoRule horarioRule;

	@BeforeEach
	void setUp() {
		pacienteRule = new CriarPacienteIdDeveSerInformadoRule();
		telefoneRule = new CriarTelefoneDevePreenchidoRule();
		medicamentoRule = new CriarMedicamentoDevePreenchidoRule();
		doseRule = new CriarDoseDevePreenchidaRule();
		horarioRule = new CriarHorarioAlvoDeveSerInformadoRule();
	}

	private CriacaoAlarmeContext contexto(CriarAlarmeInput input) {
		return new CriacaoAlarmeContext(input);
	}

	@Test
	void devePassarQuandoTudoValido() {
		CriacaoAlarmeContext contexto = contexto(umaCriacaoValida());

		assertDoesNotThrow(() -> pacienteRule.validar(contexto));
		assertDoesNotThrow(() -> telefoneRule.validar(contexto));
		assertDoesNotThrow(() -> medicamentoRule.validar(contexto));
		assertDoesNotThrow(() -> doseRule.validar(contexto));
		assertDoesNotThrow(() -> horarioRule.validar(contexto));
	}

	@Test
	void deveRejeitarPacienteIdNulo() {
		assertThrows(
				PacienteIdObrigatorioException.class,
				() -> pacienteRule.validar(contexto(umaCriacaoCom(null, TELEFONE, MEDICAMENTO, DOSE, INSTANTE_FIXO))));
	}

	@Test
	void deveRejeitarTelefoneNuloOuEmBranco() {
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(contexto(umaCriacaoCom(PACIENTE_ID, null, MEDICAMENTO, DOSE, INSTANTE_FIXO))));
		assertThrows(
				TelefoneObrigatorioException.class,
				() -> telefoneRule.validar(
						contexto(umaCriacaoCom(PACIENTE_ID, VALOR_EM_BRANCO, MEDICAMENTO, DOSE, INSTANTE_FIXO))));
	}

	@Test
	void deveRejeitarMedicamentoNuloOuEmBranco() {
		assertThrows(
				MedicamentoObrigatorioException.class,
				() -> medicamentoRule.validar(contexto(umaCriacaoCom(PACIENTE_ID, TELEFONE, null, DOSE, INSTANTE_FIXO))));
		assertThrows(
				MedicamentoObrigatorioException.class,
				() -> medicamentoRule.validar(
						contexto(umaCriacaoCom(PACIENTE_ID, TELEFONE, VALOR_EM_BRANCO, DOSE, INSTANTE_FIXO))));
	}

	@Test
	void deveRejeitarDoseNulaOuEmBranco() {
		assertThrows(
				DoseObrigatoriaException.class,
				() -> doseRule.validar(contexto(umaCriacaoCom(PACIENTE_ID, TELEFONE, MEDICAMENTO, null, INSTANTE_FIXO))));
		assertThrows(
				DoseObrigatoriaException.class,
				() -> doseRule.validar(
						contexto(umaCriacaoCom(PACIENTE_ID, TELEFONE, MEDICAMENTO, VALOR_EM_BRANCO, INSTANTE_FIXO))));
	}

	@Test
	void deveRejeitarHorarioAlvoNulo() {
		assertThrows(
				HorarioAlvoObrigatorioException.class,
				() -> horarioRule.validar(contexto(umaCriacaoCom(PACIENTE_ID, TELEFONE, MEDICAMENTO, DOSE, null))));
	}
}
