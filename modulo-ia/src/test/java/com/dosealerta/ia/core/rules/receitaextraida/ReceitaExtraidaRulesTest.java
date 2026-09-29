package com.dosealerta.ia.core.rules.receitaextraida;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReceitaExtraidaRulesTest extends TesteUnitarioBase {

	private static final MedicamentoExtraido LOSARTANA = new MedicamentoExtraido("Losartana", "50mg", 24, 30);
	private static final String PRESCRITOR = "Dr. João";
	private static final String REGISTRO_VALIDO = "CRM 70760";

	private ReceitaExtraidaDeveSerReceitaMedicaRule medicaRule;
	private ReceitaExtraidaDeveTerNomeDoPrescritorRule prescritorRule;
	private ReceitaExtraidaDeveTerRegistroProfissionalPlausivelRule registroRule;
	private ReceitaExtraidaDeveTerMedicamentosRule medicamentosRule;

	@BeforeEach
	void setUp() {
		medicaRule = new ReceitaExtraidaDeveSerReceitaMedicaRule();
		prescritorRule = new ReceitaExtraidaDeveTerNomeDoPrescritorRule();
		registroRule = new ReceitaExtraidaDeveTerRegistroProfissionalPlausivelRule();
		medicamentosRule = new ReceitaExtraidaDeveTerMedicamentosRule();
	}

	private ReceitaExtraidaContext contexto(boolean receitaMedica, String nome, String registro) {
		return new ReceitaExtraidaContext(new ReceitaExtraida(receitaMedica, nome, registro, List.of(LOSARTANA)));
	}

	@Test
	void devePassarParaReceitaFormalDeMedicoOuDentista() {
		ReceitaExtraidaContext contexto = contexto(true, PRESCRITOR, REGISTRO_VALIDO);

		assertDoesNotThrow(() -> medicaRule.validar(contexto));
		assertDoesNotThrow(() -> prescritorRule.validar(contexto));
		assertDoesNotThrow(() -> registroRule.validar(contexto));
		assertDoesNotThrow(() -> medicamentosRule.validar(contexto));
	}

	@Test
	void deveAceitarRegistroEmFormatosComuns() {
		for (String registro : new String[] {"70760", "CRM: 70.760", "CRM-SP 123456", "CRM/SP 1234", "CRO/SC 99999"}) {
			assertDoesNotThrow(() -> registroRule.validar(contexto(true, PRESCRITOR, registro)));
		}
	}

	@Test
	void deveRejeitarImagemQueNaoEReceitaComAOrientacao() {
		ReceitaFormalNaoIdentificadaException e = assertThrows(
				ReceitaFormalNaoIdentificadaException.class,
				() -> medicaRule.validar(contexto(false, PRESCRITOR, REGISTRO_VALIDO)));

		assertEquals(ReceitaFormalNaoIdentificadaException.ORIENTACAO, e.getMessage());
	}

	@Test
	void deveRejeitarReceitaSemNomeDoPrescritor() {
		for (String nome : new String[] {null, "", "  "}) {
			assertThrows(
					ReceitaFormalNaoIdentificadaException.class,
					() -> prescritorRule.validar(contexto(true, nome, REGISTRO_VALIDO)));
		}
	}

	@Test
	void deveRejeitarReceitaSemRegistroOuComRegistroImplausivel() {
		for (String registro : new String[] {null, "", "CRM", "123", "12345678"}) {
			assertThrows(
					ReceitaFormalNaoIdentificadaException.class,
					() -> registroRule.validar(contexto(true, PRESCRITOR, registro)),
					"deveria rejeitar: '" + registro + "'");
		}
	}

	@Test
	void deveRejeitarReceitaSemNenhumMedicamento() {
		assertThrows(
				ReceitaInvalidaException.class,
				() -> medicamentosRule.validar(
						new ReceitaExtraidaContext(new ReceitaExtraida(true, PRESCRITOR, REGISTRO_VALIDO, List.of()))));
		assertThrows(
				ReceitaInvalidaException.class,
				() -> medicamentosRule.validar(
						new ReceitaExtraidaContext(new ReceitaExtraida(true, PRESCRITOR, REGISTRO_VALIDO, null))));
	}
}
