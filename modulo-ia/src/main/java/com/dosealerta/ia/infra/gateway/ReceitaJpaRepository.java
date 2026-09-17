package com.dosealerta.ia.infra.gateway;

import com.dosealerta.ia.infra.gateway.entity.ReceitaJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ReceitaJpaRepository extends JpaRepository<ReceitaJpaEntity, UUID> {
}
