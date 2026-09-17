package com.dosealerta.ia.infra.gateway;

import com.dosealerta.ia.infra.gateway.entity.FeedbackExtracaoJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface FeedbackExtracaoJpaRepository extends JpaRepository<FeedbackExtracaoJpaEntity, UUID> {
}
