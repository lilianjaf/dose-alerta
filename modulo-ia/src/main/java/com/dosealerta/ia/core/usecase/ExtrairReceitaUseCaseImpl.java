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
import com.dosealerta.ia.core.gateway.TransactionGateway;
import com.dosealerta.ia.core.rules.RegraValidacaoReceitaExtraida;
import com.dosealerta.ia.core.rules.extrair.ExtracaoReceitaContext;
import com.dosealerta.ia.core.rules.extrair.ValidadorExtracaoReceitaRule;
import com.dosealerta.ia.core.rules.receitaextraida.ReceitaExtraidaContext;
import com.dosealerta.ia.core.rules.receitaextraida.ValidadorReceitaExtraidaRule;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

public class ExtrairReceitaUseCaseImpl implements ExtrairReceitaUseCase {

	private final ExtratorReceitaGateway extratorReceitaGateway;
	private final ReceitaRepositoryGateway receitaRepositoryGateway;
	private final TransactionGateway transactionGateway;
	private final Clock clock;
	private final List<ValidadorExtracaoReceitaRule> regrasDeEntrada;
	private final List<ValidadorReceitaExtraidaRule> regrasDaReceitaExtraida;

	public ExtrairReceitaUseCaseImpl(ExtratorReceitaGateway extratorReceitaGateway, ReceitaRepositoryGateway receitaRepositoryGateway, TransactionGateway transactionGateway, Clock clock, List<ValidadorExtracaoReceitaRule> regrasDeEntrada, List<ValidadorReceitaExtraidaRule> regrasDaReceitaExtraida) {
		this.extratorReceitaGateway = extratorReceitaGateway;
		this.receitaRepositoryGateway = receitaRepositoryGateway;
		this.transactionGateway = transactionGateway;
		this.clock = clock;
		this.regrasDeEntrada = regrasDeEntrada;
		this.regrasDaReceitaExtraida = regrasDaReceitaExtraida;
	}

	@Override
	public ResultadoExtracao executar(ExtrairReceitaInput input) {
		ExtracaoReceitaContext contextoEntrada = new ExtracaoReceitaContext(input);
		regrasDeEntrada.forEach(rule -> rule.validar(contextoEntrada));

		ReceitaExtraida extraida = extratorReceitaGateway.extrair(input.imagem());
		ReceitaExtraidaContext contextoExtraida = new ReceitaExtraidaContext(extraida);
		regrasDaReceitaExtraida.forEach(rule -> rule.validar(contextoExtraida));

		return montarResultado(input, extraida);
	}

	private ResultadoExtracao montarResultado(ExtrairReceitaInput input, ReceitaExtraida extraida) {
		List<Receita> receitas = new ArrayList<>();
		List<MedicamentoNaoProcessado> naoProcessados = new ArrayList<>();
		ReceitaInvalidaException primeiroMotivo = null;

		for (MedicamentoExtraido lido : extraida.medicamentos()) {
			try {
				MedicamentoExtraido medicamento = RegraValidacaoReceitaExtraida.normalizarMedicamento(lido);
				receitas.add(novaReceita(input, medicamento));
			} catch (ReceitaInvalidaException e) {
				primeiroMotivo = primeiroMotivo == null ? e : primeiroMotivo;
				naoProcessados.add(new MedicamentoNaoProcessado(lido.medicamento(), e.getMotivo()));
			}
		}

		if (receitas.isEmpty()) {
			throw primeiroMotivo;
		}
		List<Receita> salvas = transactionGateway.execute(() -> receitaRepositoryGateway.salvarTodas(receitas));
		return new ResultadoExtracao(salvas, naoProcessados);
	}

	private Receita novaReceita(ExtrairReceitaInput input, MedicamentoExtraido medicamento) {
		return Receita.aguardandoConfirmacao(
				input.pacienteId(),
				input.telefone(),
				medicamento.medicamento(),
				medicamento.dose(),
				medicamento.frequenciaHoras(),
				medicamento.duracaoDias(),
				input.horarioInicial(),
				clock.instant());
	}
}
