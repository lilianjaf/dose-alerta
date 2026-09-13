package com.dosealerta.usuario.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;
import com.dosealerta.usuario.core.gateway.AutenticacaoGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AutenticarPacienteUseCaseTest {

	@Mock
	private PacienteRepositoryGateway pacienteRepositoryGateway;

	@Mock
	private SenhaGateway senhaGateway;

	@Mock
	private AutenticacaoGateway autenticacaoGateway;

	private AutenticarPacienteUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new AutenticarPacienteUseCase(pacienteRepositoryGateway, senhaGateway, autenticacaoGateway);
	}

	@Test
	void deveAutenticarERetornarTokenQuandoCredenciaisValidas() {
		var input = new AutenticarPacienteInput("+5511999999999", "senha1234");
		Paciente paciente = Paciente.novo("Maria", input.telefone(), "hash-armazenado");
		when(pacienteRepositoryGateway.buscarPorTelefone(input.telefone())).thenReturn(Optional.of(paciente));
		when(senhaGateway.confere(input.senha(), "hash-armazenado")).thenReturn(true);
		when(autenticacaoGateway.emitirToken(any(Paciente.class))).thenReturn("token-fake");

		var resultado = useCase.executar(input);

		assertEquals("token-fake", resultado.token());
	}

	@Test
	void deveRejeitarQuandoTelefoneNaoExiste() {
		var input = new AutenticarPacienteInput("+5511999999999", "senha1234");
		when(pacienteRepositoryGateway.buscarPorTelefone(input.telefone())).thenReturn(Optional.empty());

		assertThrows(CredenciaisInvalidasException.class, () -> useCase.executar(input));
	}

	@Test
	void deveRejeitarQuandoSenhaNaoConfere() {
		var input = new AutenticarPacienteInput("+5511999999999", "senha-errada");
		Paciente paciente = Paciente.novo("Maria", input.telefone(), "hash-armazenado");
		when(pacienteRepositoryGateway.buscarPorTelefone(input.telefone())).thenReturn(Optional.of(paciente));
		when(senhaGateway.confere(input.senha(), "hash-armazenado")).thenReturn(false);

		assertThrows(CredenciaisInvalidasException.class, () -> useCase.executar(input));
	}
}
