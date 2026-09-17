package com.dosealerta.scheduler.infra.controller;

import com.dosealerta.scheduler.core.dto.AlarmeOutput;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.dto.RegistrarInteracaoPorTelefoneInput;
import com.dosealerta.scheduler.core.usecase.BuscarAlarmeUseCase;
import com.dosealerta.scheduler.core.usecase.CriarAlarmeUseCase;
import com.dosealerta.scheduler.core.usecase.RegistrarConfirmacaoUseCase;
import com.dosealerta.scheduler.core.usecase.RegistrarLigacaoAtendidaUseCase;
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
	private final RegistrarConfirmacaoUseCase registrarConfirmacaoUseCase;
	private final RegistrarLigacaoAtendidaUseCase registrarLigacaoAtendidaUseCase;

	public AlarmeController(
			CriarAlarmeUseCase criarAlarmeUseCase,
			BuscarAlarmeUseCase buscarAlarmeUseCase,
			RegistrarConfirmacaoUseCase registrarConfirmacaoUseCase,
			RegistrarLigacaoAtendidaUseCase registrarLigacaoAtendidaUseCase) {
		this.criarAlarmeUseCase = criarAlarmeUseCase;
		this.buscarAlarmeUseCase = buscarAlarmeUseCase;
		this.registrarConfirmacaoUseCase = registrarConfirmacaoUseCase;
		this.registrarLigacaoAtendidaUseCase = registrarLigacaoAtendidaUseCase;
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

	@PostMapping("/alarmes/confirmacoes")
	public AlarmeOutput confirmar(@Valid @RequestBody RegistrarInteracaoPorTelefoneInput input) {
		return AlarmeOutput.de(registrarConfirmacaoUseCase.executar(input.telefone()));
	}

	@PostMapping("/alarmes/ligacoes/atendidas")
	public AlarmeOutput registrarLigacaoAtendida(@Valid @RequestBody RegistrarInteracaoPorTelefoneInput input) {
		return AlarmeOutput.de(registrarLigacaoAtendidaUseCase.executar(input.telefone()));
	}
}
