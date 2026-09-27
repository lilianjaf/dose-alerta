package com.dosealerta.notificacao.infra.gateway;

import com.dosealerta.notificacao.core.domain.StatusOutboxEvent;
import com.dosealerta.notificacao.infra.gateway.entity.OutboxEventJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

	@Query("""
			select e from OutboxEventJpaEntity e
			where e.status = :status and (e.proximaTentativaEm is null or e.proximaTentativaEm <= :agora)
			order by e.criadoEm asc""")
	List<OutboxEventJpaEntity> buscarVencidos(
			@Param("status") StatusOutboxEvent status, @Param("agora") Instant agora, Pageable pageable);
}
