package com.dosealerta.ia.infra.client;

import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import java.util.List;

/**
 * Substitui a chamada real ao Gemini por dados fixos, para testar o resto do fluxo (confirmação, criação de
 * alarme) sem depender do modelo de visão estar disponível — útil quando o Gemini está fora do ar ou sobrecarregado
 * (ver {@link GeminiExtratorReceitaGateway}). Devolve um medicamento completo e um com campos pendentes, pra
 * exercitar as duas confirmações possíveis.
 *
 * <p>Não é um {@code @Component}: só é instanciado explicitamente para o endpoint {@code /receitas/extrair-mock}
 * (ver {@code UseCaseConfig}), nunca para a extração de verdade.
 */
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
