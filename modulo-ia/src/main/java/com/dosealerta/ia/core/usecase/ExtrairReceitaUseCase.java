package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.rules.RegraValidacaoReceitaExtraida;

public class ExtrairReceitaUseCase {

	private final ExtratorReceitaGateway extratorReceitaGateway;
	private final ReceitaRepositoryGateway receitaRepositoryGateway;

	public ExtrairReceitaUseCase(
			ExtratorReceitaGateway extratorReceitaGateway, ReceitaRepositoryGateway receitaRepositoryGateway) {
		this.extratorReceitaGateway = extratorReceitaGateway;
		this.receitaRepositoryGateway = receitaRepositoryGateway;
	}

	public Receita executar(ExtrairReceitaInput input) {
		ReceitaExtraida extraida = extratorReceitaGateway.extrair(input.imagem());
		RegraValidacaoReceitaExtraida.validar(extraida);

		Receita receita = Receita.aguardandoConfirmacao(
				input.pacienteId(),
				input.telefone(),
				extraida.medicamento(),
				extraida.dose(),
				extraida.frequenciaHoras(),
				extraida.duracaoDias(),
				input.horarioInicial());

		return receitaRepositoryGateway.salvar(receita);
	}
}
