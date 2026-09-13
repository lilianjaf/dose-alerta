package com.dosealerta.scheduler.infra.controller;

import com.dosealerta.scheduler.core.dto.AlarmeOutput;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.usecase.BuscarAlarmeUseCase;
import com.dosealerta.scheduler.core.usecase.CriarAlarmeUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlarmeController {

	private final CriarAlarmeUseCase criarAlarmeUseCase;
	private final BuscarAlarmeUseCase buscarAlarmeUseCase;

	public AlarmeController(CriarAlarmeUseCase criarAlarmeUseCase, BuscarAlarmeUseCase buscarAlarmeUseCase) {
		this.criarAlarmeUseCase = criarAlarmeUseCase;
		this.buscarAlarmeUseCase = buscarAlarmeUseCase;
	}

	@PostMapping("/alarmes")
	public ResponseEntity<AlarmeOutput> criar(@Valid @RequestBody CriarAlarmeInput input) {
		AlarmeOutput output = AlarmeOutput.de(criarAlarmeUseCase.executar(input));
		return ResponseEntity.status(HttpStatus.CREATED).body(output);
	}

	@GetMapping("/alarmes/{id}")
	public AlarmeOutput buscar(@PathVariable UUID id) {
		return AlarmeOutput.de(buscarAlarmeUseCase.executar(id));
	}
}
