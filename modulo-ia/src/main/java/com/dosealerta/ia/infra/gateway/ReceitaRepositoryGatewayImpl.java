package com.dosealerta.ia.infra.gateway;

import com.dosealerta.ia.core.domain.Receita;
import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.infra.gateway.mapper.ReceitaMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class ReceitaRepositoryGatewayImpl implements ReceitaRepositoryGateway {

	private final ReceitaJpaRepository receitaJpaRepository;

	ReceitaRepositoryGatewayImpl(ReceitaJpaRepository receitaJpaRepository) {
		this.receitaJpaRepository = receitaJpaRepository;
	}

	@Override
	public Receita salvar(Receita receita) {
		var entidade = receitaJpaRepository.save(ReceitaMapper.paraEntidade(receita));
		return ReceitaMapper.paraDominio(entidade);
	}

	@Override
	public List<Receita> salvarTodas(List<Receita> receitas) {
		return receitas.stream().map(this::salvar).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Receita> buscarPorId(UUID id) {
		return receitaJpaRepository.findById(id).map(ReceitaMapper::paraDominio);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Receita> buscarAguardandoConfirmacaoMaisRecentePorTelefone(String telefone) {
		return receitaJpaRepository
				.findFirstByTelefoneAndStatusOrderByCriadoEmDesc(telefone, StatusReceita.AGUARDANDO_CONFIRMACAO)
				.map(ReceitaMapper::paraDominio);
	}
}
