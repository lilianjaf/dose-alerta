package com.dosealerta.usuario.infra.controller;

import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.dto.PacienteOutput;
import com.dosealerta.usuario.core.usecase.CadastrarPacienteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PacienteController {

	private final CadastrarPacienteUseCase cadastrarPacienteUseCase;

	public PacienteController(CadastrarPacienteUseCase cadastrarPacienteUseCase) {
		this.cadastrarPacienteUseCase = cadastrarPacienteUseCase;
	}

	@PostMapping("/pacientes")
	public ResponseEntity<PacienteOutput> cadastrar(@Valid @RequestBody CadastrarPacienteInput input) {
		PacienteOutput output = PacienteOutput.de(cadastrarPacienteUseCase.executar(input));
		return ResponseEntity.status(HttpStatus.CREATED).body(output);
	}
}
