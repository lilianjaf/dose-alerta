package com.dosealerta.notificacao.infra.gateway;

import com.dosealerta.notificacao.core.domain.StatusOutboxEvent;
import com.dosealerta.notificacao.infra.gateway.entity.OutboxEventJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

	List<OutboxEventJpaEntity> findByStatusOrderByCriadoEmAsc(StatusOutboxEvent status, Pageable pageable);
}
