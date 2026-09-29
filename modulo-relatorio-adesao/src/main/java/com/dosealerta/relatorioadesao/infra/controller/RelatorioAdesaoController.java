package com.dosealerta.relatorioadesao.infra.controller;

import com.dosealerta.relatorioadesao.core.dto.TaxaAdesaoOutput;
import com.dosealerta.relatorioadesao.core.usecase.ConsultarTaxaAdesaoUseCase;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RelatorioAdesaoController {

	private final ConsultarTaxaAdesaoUseCase consultarTaxaAdesaoUseCase;

	RelatorioAdesaoController(ConsultarTaxaAdesaoUseCase consultarTaxaAdesaoUseCase) {
		this.consultarTaxaAdesaoUseCase = consultarTaxaAdesaoUseCase;
	}

	@GetMapping("/pacientes/{pacienteId}/adesao")
	List<TaxaAdesaoOutput> consultar(
			@PathVariable UUID pacienteId,
			@RequestParam(required = false) Instant inicio,
			@RequestParam(required = false) Instant fim) {
		return consultarTaxaAdesaoUseCase.executar(pacienteId, inicio, fim).stream()
				.map(TaxaAdesaoOutput::de)
				.toList();
	}
}
