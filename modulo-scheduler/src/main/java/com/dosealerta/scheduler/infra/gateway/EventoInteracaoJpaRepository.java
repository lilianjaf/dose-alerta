package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.domain.StatusOutboxEvent;
import com.dosealerta.scheduler.infra.gateway.entity.EventoInteracaoJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

interface EventoInteracaoJpaRepository extends JpaRepository<EventoInteracaoJpaEntity, UUID> {

	List<EventoInteracaoJpaEntity> findByStatusOrderByRegistradaEmAsc(StatusOutboxEvent status, PageRequest pageRequest);
}
