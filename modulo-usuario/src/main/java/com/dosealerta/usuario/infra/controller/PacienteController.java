package com.dosealerta.usuario.infra.controller;

import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteOutput;
import com.dosealerta.usuario.core.dto.PacienteOutput;
import com.dosealerta.usuario.core.usecase.CadastrarPacienteUseCase;
import com.dosealerta.usuario.core.usecase.CompletarCadastroUseCase;
import com.dosealerta.usuario.core.usecase.IdentificarPacientePorTelefoneUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PacienteController {

	private final CadastrarPacienteUseCase cadastrarPacienteUseCase;
	private final IdentificarPacientePorTelefoneUseCase identificarPacientePorTelefoneUseCase;
	private final CompletarCadastroUseCase completarCadastroUseCase;

	public PacienteController(
			CadastrarPacienteUseCase cadastrarPacienteUseCase,
			IdentificarPacientePorTelefoneUseCase identificarPacientePorTelefoneUseCase,
			CompletarCadastroUseCase completarCadastroUseCase) {
		this.cadastrarPacienteUseCase = cadastrarPacienteUseCase;
		this.identificarPacientePorTelefoneUseCase = identificarPacientePorTelefoneUseCase;
		this.completarCadastroUseCase = completarCadastroUseCase;
	}

	@PostMapping("/pacientes")
	public ResponseEntity<PacienteOutput> cadastrar(@RequestBody CadastrarPacienteInput input) {
		PacienteOutput output = PacienteOutput.de(cadastrarPacienteUseCase.executar(input));
		return ResponseEntity.status(HttpStatus.CREATED).body(output);
	}

	@PostMapping("/pacientes/identificar")
	public IdentificarPacienteOutput identificar(@RequestBody IdentificarPacienteInput input) {
		return identificarPacientePorTelefoneUseCase.executar(input);
	}

	@PostMapping("/pacientes/completar-cadastro")
	public ResponseEntity<PacienteOutput> completarCadastro(@RequestBody CompletarCadastroInput input) {
		return ResponseEntity.ok(PacienteOutput.de(completarCadastroUseCase.executar(input)));
	}
}
