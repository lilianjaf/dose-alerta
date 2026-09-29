package com.dosealerta.relatorioadesao.core.usecase;

import com.dosealerta.relatorioadesao.core.domain.TaxaAdesao;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ConsultarTaxaAdesaoUseCase {

	List<TaxaAdesao> executar(UUID pacienteId, Instant inicio, Instant fim);
}
