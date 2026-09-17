package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import com.dosealerta.scheduler.core.exception.ConflitoConcorrenciaException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import com.dosealerta.scheduler.infra.gateway.mapper.AlarmeMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class AlarmeRepositoryGatewayImpl implements AlarmeRepositoryGateway {

	private final AlarmeJpaRepository alarmeJpaRepository;

	AlarmeRepositoryGatewayImpl(AlarmeJpaRepository alarmeJpaRepository) {
		this.alarmeJpaRepository = alarmeJpaRepository;
	}

	@Override
	public Alarme salvar(Alarme alarme) {
		try {
			var entidade = alarmeJpaRepository.save(AlarmeMapper.paraEntidade(alarme));
			return AlarmeMapper.paraDominio(entidade);
		} catch (ObjectOptimisticLockingFailureException e) {
			throw new ConflitoConcorrenciaException(alarme.getId(), e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Alarme> buscarPorId(UUID id) {
		return alarmeJpaRepository.findById(id).map(AlarmeMapper::paraDominio);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Alarme> buscarPendentesParaEscalonamento() {
		return alarmeJpaRepository.findByStatus(StatusAlarme.PENDENTE).stream()
				.map(AlarmeMapper::paraDominio)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Alarme> buscarPendenteMaisRecentePorTelefone(String telefone) {
		return alarmeJpaRepository
				.findFirstByTelefoneAndStatusAndEtapaAtualIsNotNullOrderByUltimoEnvioEmDesc(
						telefone, StatusAlarme.PENDENTE)
				.map(AlarmeMapper::paraDominio);
	}
}
