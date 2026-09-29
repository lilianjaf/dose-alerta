package com.dosealerta.ia.infra.client;

import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import java.util.List;

public class MockExtratorReceitaGateway implements ExtratorReceitaGateway {

	private static final String PRESCRITOR_MOCK = "Dr. Exemplo da Silva";
	private static final String REGISTRO_MOCK = "CRM 12.345";
	private static final String MEDICAMENTO_LOSARTANA_MOCK = "Losartana 50mg";
	private static final String DOSE_LOSARTANA_MOCK = "1 comprimido";
	private static final String MEDICAMENTO_AMOXICILINA_MOCK = "Amoxicilina 500mg";

	@Override
	public ReceitaExtraida extrair(byte[] imagem) {
		return new ReceitaExtraida(
				true,
				PRESCRITOR_MOCK,
				REGISTRO_MOCK,
				List.of(
						new MedicamentoExtraido(MEDICAMENTO_LOSARTANA_MOCK, DOSE_LOSARTANA_MOCK, 24, 30),
						new MedicamentoExtraido(MEDICAMENTO_AMOXICILINA_MOCK, null, 8, null)));
	}
}
