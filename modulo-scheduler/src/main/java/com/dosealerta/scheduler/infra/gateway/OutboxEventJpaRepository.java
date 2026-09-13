package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.domain.StatusOutboxEvent;
import com.dosealerta.scheduler.infra.gateway.entity.OutboxEventJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

	List<OutboxEventJpaEntity> findByStatusOrderByCriadoEmAsc(StatusOutboxEvent status, Pageable pageable);
}
