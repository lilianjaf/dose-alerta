package com.dosealerta.ia.core.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.TesteUnitarioBase;
import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import org.junit.jupiter.api.Test;

class RegraValidacaoReceitaExtraidaTest extends TesteUnitarioBase {

	private static final MedicamentoExtraido LOSARTANA = new MedicamentoExtraido("Losartana", "50mg", 24, 30);

	@Test
	void deveManterOsDadosDeUmMedicamentoValido() {
		assertEquals(LOSARTANA, RegraValidacaoReceitaExtraida.normalizarMedicamento(LOSARTANA));
	}

	@Test
	void deveRejeitarMedicamentoSemNome() {
		for (String nome : new String[] {null, "", "  "}) {
			assertThrows(
					ReceitaInvalidaException.class,
					() -> RegraValidacaoReceitaExtraida.normalizarMedicamento(
							new MedicamentoExtraido(nome, "50mg", 24, 30)));
		}
	}

	@Test
	void deveAceitarDoseComVariasUnidadesConhecidas() {
		for (String dose : new String[] {
			"1 comprimido", "10 UI", "5 gotas", "2 doses", "2 jatos", "1 puff", "2 inalações", "1 aplicação",
			"2 aplicações", "1 cp", "1cp", "2 cps", "1 comp", "1 cap", "20 gts", "1 amp", "1/2 comprimido",
			"meio comprimido", "2,5 mg", "0.5 ml", "1 sachê", "1 CP", "1 Cápsula"
		}) {
			assertEquals(
					dose,
					RegraValidacaoReceitaExtraida.normalizarMedicamento(new MedicamentoExtraido("Remedio", dose, 8, 5))
							.dose());
		}
	}

	@Test
	void deveAnularDoseAusenteOuForaDoFormato() {
		for (String dose : new String[] {null, "", "cp", "muito", "1 fusca", "50", "uso ambulatorial"}) {
			MedicamentoExtraido normalizado = RegraValidacaoReceitaExtraida.normalizarMedicamento(
					new MedicamentoExtraido("Remedio", dose, 8, 5));

			assertNull(normalizado.dose(), "deveria anular: '" + dose + "'");
			assertEquals(8, normalizado.frequenciaHoras());
			assertEquals(5, normalizado.duracaoDias());
		}
	}

	@Test
	void deveAceitarFrequenciasDeUmaHoraAUmaSemana() {
		for (int frequencia : new int[] {1, 8, 84, 168}) {
			assertEquals(
					frequencia,
					RegraValidacaoReceitaExtraida.normalizarMedicamento(
									new MedicamentoExtraido("Remedio", "1 cp", frequencia, 5))
							.frequenciaHoras());
		}
	}

	@Test
	void deveAnularFrequenciaAusenteOuForaDaFaixa() {
		for (Integer frequencia : new Integer[] {null, 0, -1, 169, 1000}) {
			assertNull(
					RegraValidacaoReceitaExtraida.normalizarMedicamento(
									new MedicamentoExtraido("Remedio", "50mg", frequencia, 30))
							.frequenciaHoras(),
					"deveria anular: " + frequencia);
		}
	}

	@Test
	void deveDeixarADuracaoNulaQuandoAusenteSemInventarUmValor() {
		assertNull(RegraValidacaoReceitaExtraida.normalizarMedicamento(
						new MedicamentoExtraido("Remedio", "50mg", 24, null))
				.duracaoDias());
	}

	@Test
	void deveAnularDuracaoForaDaFaixa() {
		for (int duracao : new int[] {0, -5, 366}) {
			assertNull(
					RegraValidacaoReceitaExtraida.normalizarMedicamento(
									new MedicamentoExtraido("Remedio", "50mg", 24, duracao))
							.duracaoDias(),
					"deveria anular: " + duracao + " dias");
		}
	}

}
