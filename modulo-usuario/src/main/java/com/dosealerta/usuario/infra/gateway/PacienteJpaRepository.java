package com.dosealerta.usuario.infra.gateway;

import com.dosealerta.usuario.infra.gateway.entity.PacienteJpaEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PacienteJpaRepository extends JpaRepository<PacienteJpaEntity, UUID> {

	Optional<PacienteJpaEntity> findByTelefone(String telefone);
}
