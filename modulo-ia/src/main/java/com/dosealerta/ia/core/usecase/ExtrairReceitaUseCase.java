package com.dosealerta.ia.core.usecase;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.dto.ExtrairReceitaInput;
import com.dosealerta.ia.core.dto.MedicamentoExtraido;
import com.dosealerta.ia.core.dto.MedicamentoNaoProcessado;
import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.dto.ResultadoExtracao;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.rules.RegraValidacaoReceitaExtraida;
import java.util.ArrayList;
import java.util.List;

public class ExtrairReceitaUseCase {

	private final ExtratorReceitaGateway extratorReceitaGateway;
	private final ReceitaRepositoryGateway receitaRepositoryGateway;

	public ExtrairReceitaUseCase(
			ExtratorReceitaGateway extratorReceitaGateway, ReceitaRepositoryGateway receitaRepositoryGateway) {
		this.extratorReceitaGateway = extratorReceitaGateway;
		this.receitaRepositoryGateway = receitaRepositoryGateway;
	}

	/**
	 * Cria uma receita aguardando confirmação para cada medicamento lido. Dose, frequência e duração que a
	 * receita não trouxe ficam nulas (ver {@code camposPendentes} em cada receita) e são exigidas na confirmação,
	 * que é o que impede o alarme de ser criado sem elas. Só o item sem nome legível não vira receita: vai em
	 * {@code naoProcessados}. Se nenhum medicamento puder ser listado, a extração inteira é reprovada.
	 */
	public ResultadoExtracao executar(ExtrairReceitaInput input) {
		ReceitaExtraida extraida = extratorReceitaGateway.extrair(input.imagem());
		RegraValidacaoReceitaExtraida.validarReceitaFormal(extraida);

		List<Receita> receitas = new ArrayList<>();
		List<MedicamentoNaoProcessado> naoProcessados = new ArrayList<>();
		ReceitaInvalidaException primeiroMotivo = null;

		for (MedicamentoExtraido lido : extraida.medicamentos()) {
			MedicamentoExtraido medicamento;
			try {
				medicamento = RegraValidacaoReceitaExtraida.normalizarMedicamento(lido);
			} catch (ReceitaInvalidaException e) {
				primeiroMotivo = primeiroMotivo == null ? e : primeiroMotivo;
				naoProcessados.add(new MedicamentoNaoProcessado(lido.medicamento(), e.getMotivo()));
				continue;
			}
			receitas.add(Receita.aguardandoConfirmacao(
					input.pacienteId(),
					input.telefone(),
					medicamento.medicamento(),
					medicamento.dose(),
					medicamento.frequenciaHoras(),
					medicamento.duracaoDias(),
					input.horarioInicial()));
		}

		if (receitas.isEmpty()) {
			throw primeiroMotivo;
		}
		return new ResultadoExtracao(receitaRepositoryGateway.salvarTodas(receitas), naoProcessados);
	}
}
