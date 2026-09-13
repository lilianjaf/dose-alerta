package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.domain.StatusAlarme;
import com.dosealerta.scheduler.infra.gateway.entity.AlarmeJpaEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AlarmeJpaRepository extends JpaRepository<AlarmeJpaEntity, UUID> {

	List<AlarmeJpaEntity> findByStatus(StatusAlarme status);
}
