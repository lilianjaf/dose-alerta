package com.dosealerta.ia;

import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import java.util.List;

public final class ReceitaExtraidaFixtures {

	public static final String PRESCRITOR = "Dra. Exemplo";
	public static final String REGISTRO = "CRM 70.760";

	private ReceitaExtraidaFixtures() {
	}

	public static ReceitaExtraida umMedicamento(String medicamento, String dose, Integer frequenciaHoras, Integer duracaoDias) {
		return comMedicamentos(new MedicamentoExtraido(medicamento, dose, frequenciaHoras, duracaoDias));
	}

	public static ReceitaExtraida comMedicamentos(MedicamentoExtraido... medicamentos) {
		return new ReceitaExtraida(true, PRESCRITOR, REGISTRO, List.of(medicamentos));
	}

	public static ReceitaExtraida semRegistroProfissional() {
		return new ReceitaExtraida(
				true, PRESCRITOR, null, List.of(new MedicamentoExtraido("Losartana", "50mg", 24, 30)));
	}

	public static ReceitaExtraida somenteDecadronSemFrequenciaEDuracao() {
		return comMedicamentos(new MedicamentoExtraido("Decadron 4mg", "2 comprimidos", null, null));
	}

	public static ReceitaExtraida comVariosMedicamentosIncluindoUmSemNome() {
		return comMedicamentos(
				new MedicamentoExtraido("Amoxicilina 500mg", "1 comprimido", 8, 7),
				new MedicamentoExtraido("Celebra 200mg", "1 cápsula", 12, 5),
				new MedicamentoExtraido("Decadron 4mg", null, null, null),
				new MedicamentoExtraido(" ", "1 comprimido", 8, 7));
	}
}
