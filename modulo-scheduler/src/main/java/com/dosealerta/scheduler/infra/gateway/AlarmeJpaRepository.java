package com.dosealerta.scheduler.infra.gateway;

import com.dosealerta.scheduler.core.domain.StatusAlarme;
import com.dosealerta.scheduler.infra.gateway.entity.AlarmeJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface AlarmeJpaRepository extends JpaRepository<AlarmeJpaEntity, UUID> {

	List<AlarmeJpaEntity> findByStatus(StatusAlarme status);

	@Query("SELECT a FROM AlarmeJpaEntity a WHERE a.telefone = :telefone AND a.status = :status "
			+ "ORDER BY COALESCE(a.ultimoEnvioEm, a.criadoEm) DESC")
	List<AlarmeJpaEntity> buscarPendentesPorTelefoneMaisRecentePrimeiro(
			@Param("telefone") String telefone, @Param("status") StatusAlarme status);

	Optional<AlarmeJpaEntity> findFirstByPacienteIdAndStatusAndMedicamentoIgnoreCaseOrderByCriadoEmAsc(
			UUID pacienteId, StatusAlarme status, String medicamento);
}
