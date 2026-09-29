package com.dosealerta.ia.core.rules.confirmar;

import static com.dosealerta.ia.IaFixtures.DOSE;
import static com.dosealerta.ia.IaFixtures.MEDICAMENTO;
import static com.dosealerta.ia.IaFixtures.RECEITA_ID;
import static com.dosealerta.ia.IaFixtures.VALOR_EM_BRANCO;
import static com.dosealerta.ia.IaFixtures.umaReceita;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.exception.CampoInformadoEmBrancoException;
import com.dosealerta.ia.core.exception.DuracaoDiasForaDaFaixaException;
import com.dosealerta.ia.core.exception.FrequenciaHorasForaDaFaixaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConfirmarFormatoRulesTest extends TesteUnitarioBase {

	private static final int FREQUENCIA_MAXIMA = 168;
	private static final int DURACAO_MAXIMA = 365;

	private ConfirmarMedicamentoInformadoNaoDeveEstarEmBrancoRule medicamentoRule;
	private ConfirmarDoseInformadaNaoDeveEstarEmBrancoRule doseRule;
	private ConfirmarFrequenciaDeveEstarNaFaixaRule frequenciaRule;
	private ConfirmarDuracaoDeveEstarNaFaixaRule duracaoRule;

	@BeforeEach
	void setUp() {
		medicamentoRule = new ConfirmarMedicamentoInformadoNaoDeveEstarEmBrancoRule();
		doseRule = new ConfirmarDoseInformadaNaoDeveEstarEmBrancoRule();
		frequenciaRule = new ConfirmarFrequenciaDeveEstarNaFaixaRule();
		duracaoRule = new ConfirmarDuracaoDeveEstarNaFaixaRule();
	}

	private ConfirmacaoReceitaContext contexto(String medicamento, String dose, Integer frequencia, Integer duracao) {
		return new ConfirmacaoReceitaContext(RECEITA_ID, umaReceita(), medicamento, dose, frequencia, duracao);
	}

	@Test
	void devePassarComValoresValidosOuNaoInformados() {
		ConfirmacaoReceitaContext valido = contexto(MEDICAMENTO, DOSE, 24, 30);
		ConfirmacaoReceitaContext naoInformado = contexto(null, null, null, null);

		assertDoesNotThrow(() -> medicamentoRule.validar(valido));
		assertDoesNotThrow(() -> doseRule.validar(valido));
		assertDoesNotThrow(() -> frequenciaRule.validar(valido));
		assertDoesNotThrow(() -> duracaoRule.validar(valido));
		assertDoesNotThrow(() -> medicamentoRule.validar(naoInformado));
		assertDoesNotThrow(() -> doseRule.validar(naoInformado));
		assertDoesNotThrow(() -> frequenciaRule.validar(naoInformado));
		assertDoesNotThrow(() -> duracaoRule.validar(naoInformado));
	}

	@Test
	void deveRejeitarMedicamentoEDoseInformadosEmBranco() {
		assertThrows(
				CampoInformadoEmBrancoException.class,
				() -> medicamentoRule.validar(contexto(VALOR_EM_BRANCO, DOSE, 24, 30)));
		assertThrows(
				CampoInformadoEmBrancoException.class,
				() -> doseRule.validar(contexto(MEDICAMENTO, VALOR_EM_BRANCO, 24, 30)));
	}

	@Test
	void deveRejeitarFrequenciaForaDaFaixa() {
		assertThrows(
				FrequenciaHorasForaDaFaixaException.class,
				() -> frequenciaRule.validar(contexto(MEDICAMENTO, DOSE, 0, 30)));
		assertThrows(
				FrequenciaHorasForaDaFaixaException.class,
				() -> frequenciaRule.validar(contexto(MEDICAMENTO, DOSE, FREQUENCIA_MAXIMA + 1, 30)));
	}

	@Test
	void deveRejeitarDuracaoForaDaFaixa() {
		assertThrows(
				DuracaoDiasForaDaFaixaException.class, () -> duracaoRule.validar(contexto(MEDICAMENTO, DOSE, 24, 0)));
		assertThrows(
				DuracaoDiasForaDaFaixaException.class,
				() -> duracaoRule.validar(contexto(MEDICAMENTO, DOSE, 24, DURACAO_MAXIMA + 1)));
	}
}
