package com.dosealerta.ia.infra.client;

import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import java.util.List;

public class MockExtratorReceitaGateway implements ExtratorReceitaGateway {

	@Override
	public ReceitaExtraida extrair(byte[] imagem) {
		return new ReceitaExtraida(
				true,
				"Dr. Exemplo da Silva",
				"CRM 12.345",
				List.of(
						new MedicamentoExtraido("Losartana 50mg", "1 comprimido", 24, 30),
						new MedicamentoExtraido("Amoxicilina 500mg", null, 8, null)));
	}
}
