package com.dosealerta.ia.infra.gateway;

import com.dosealerta.ia.core.domain.StatusReceita;
import com.dosealerta.ia.infra.gateway.entity.ReceitaJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ReceitaJpaRepository extends JpaRepository<ReceitaJpaEntity, UUID> {

	Optional<ReceitaJpaEntity> findFirstByTelefoneAndStatusOrderByCriadoEmDesc(String telefone, StatusReceita status);
}
