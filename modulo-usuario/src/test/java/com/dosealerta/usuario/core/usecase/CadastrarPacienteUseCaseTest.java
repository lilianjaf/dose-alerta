package com.dosealerta.usuario.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CadastrarPacienteUseCaseTest {

	@Mock
	private PacienteRepositoryGateway pacienteRepositoryGateway;

	@Mock
	private SenhaGateway senhaGateway;

	private CadastrarPacienteUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new CadastrarPacienteUseCase(pacienteRepositoryGateway, senhaGateway);
	}

	@Test
	void deveCadastrarPacienteComSenhaHasheada() {
		var input = new CadastrarPacienteInput("Maria", "+5511999999999", "senha1234");
		when(pacienteRepositoryGateway.buscarPorTelefone(input.telefone())).thenReturn(Optional.empty());
		when(senhaGateway.hash(input.senha())).thenReturn("hash-fake");
		when(pacienteRepositoryGateway.salvar(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

		Paciente resultado = useCase.executar(input);

		assertEquals("Maria", resultado.getNome());
		assertEquals("+5511999999999", resultado.getTelefone());
		assertEquals("hash-fake", resultado.getSenhaHash());
		verify(pacienteRepositoryGateway).salvar(any(Paciente.class));
	}

	@Test
	void deveRejeitarTelefoneJaCadastrado() {
		var input = new CadastrarPacienteInput("Maria", "+5511999999999", "senha1234");
		when(pacienteRepositoryGateway.buscarPorTelefone(input.telefone()))
				.thenReturn(Optional.of(Paciente.novo("Outra", input.telefone(), "hash")));

		assertThrows(TelefoneJaCadastradoException.class, () -> useCase.executar(input));
	}
}
