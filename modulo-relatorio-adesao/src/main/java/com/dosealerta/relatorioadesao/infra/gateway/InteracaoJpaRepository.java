package com.dosealerta.relatorioadesao.infra.gateway;

import com.dosealerta.relatorioadesao.infra.gateway.entity.InteracaoJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface InteracaoJpaRepository extends JpaRepository<InteracaoJpaEntity, UUID> {

	List<InteracaoJpaEntity> findByPacienteIdAndRegistradaEmBetween(UUID pacienteId, Instant inicio, Instant fim);
}
