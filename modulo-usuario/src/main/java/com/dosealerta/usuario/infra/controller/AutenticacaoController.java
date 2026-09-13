package com.dosealerta.usuario.infra.controller;

import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.dto.TokenOutput;
import com.dosealerta.usuario.core.usecase.AutenticarPacienteUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

	private final AutenticarPacienteUseCase autenticarPacienteUseCase;

	public AutenticacaoController(AutenticarPacienteUseCase autenticarPacienteUseCase) {
		this.autenticarPacienteUseCase = autenticarPacienteUseCase;
	}

	@PostMapping("/login")
	public TokenOutput login(@Valid @RequestBody AutenticarPacienteInput input) {
		return autenticarPacienteUseCase.executar(input);
	}
}
