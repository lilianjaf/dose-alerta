package com.dosealerta.ia.infra.gateway;

import com.dosealerta.ia.core.domain.StatusOutboxEvent;
import com.dosealerta.ia.infra.gateway.entity.OutboxEventJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

	List<OutboxEventJpaEntity> findByStatusOrderByCriadoEmAsc(StatusOutboxEvent status, PageRequest pageRequest);
}
