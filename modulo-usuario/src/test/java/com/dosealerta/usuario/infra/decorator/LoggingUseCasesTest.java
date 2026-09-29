package com.dosealerta.usuario.infra.decorator;

import static com.dosealerta.usuario.UsuarioFixtures.umCadastroValido;
import static com.dosealerta.usuario.UsuarioFixtures.umCompletarCadastroValido;
import static com.dosealerta.usuario.UsuarioFixtures.umLoginValido;
import static com.dosealerta.usuario.UsuarioFixtures.umPaciente;
import static com.dosealerta.usuario.UsuarioFixtures.umaIdentificacaoValida;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.IdentificarPacienteOutput;
import com.dosealerta.usuario.core.dto.TokenOutput;
import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;
import com.dosealerta.usuario.core.usecase.AutenticarPacienteUseCase;
import com.dosealerta.usuario.core.usecase.CadastrarPacienteUseCase;
import com.dosealerta.usuario.core.usecase.CompletarCadastroUseCase;
import com.dosealerta.usuario.core.usecase.IdentificarPacientePorTelefoneUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class LoggingUseCasesTest extends TesteUnitarioBase {

	@Mock
	private CadastrarPacienteUseCase cadastrar;

	@Mock
	private AutenticarPacienteUseCase autenticar;

	@Mock
	private IdentificarPacientePorTelefoneUseCase identificar;

	@Mock
	private CompletarCadastroUseCase completar;

	private Paciente paciente;

	@BeforeEach
	void setUp() {
		paciente = umPaciente();
	}

	@Test
	void deveDelegarECadastrarRetornandoOResultadoDoDelegate() {
		var input = umCadastroValido();
		when(cadastrar.executar(input)).thenReturn(paciente);

		assertSame(paciente, new LoggingCadastrarPacienteUseCase(cadastrar).executar(input));
	}

	@Test
	void deveDelegarEAutenticarRetornandoOResultadoDoDelegate() {
		var input = umLoginValido();
		var token = new TokenOutput("token");
		when(autenticar.executar(input)).thenReturn(token);

		assertSame(token, new LoggingAutenticarPacienteUseCase(autenticar).executar(input));
	}

	@Test
	void deveDelegarEIdentificarRetornandoOResultadoDoDelegate() {
		var input = umaIdentificacaoValida();
		var output = IdentificarPacienteOutput.deExistente(paciente);
		when(identificar.executar(input)).thenReturn(output);

		assertSame(output, new LoggingIdentificarPacientePorTelefoneUseCase(identificar).executar(input));
	}

	@Test
	void deveDelegarECompletarRetornandoOResultadoDoDelegate() {
		var input = umCompletarCadastroValido();
		when(completar.executar(input)).thenReturn(paciente);

		assertSame(paciente, new LoggingCompletarCadastroUseCase(completar).executar(input));
	}

	@Test
	void devePropagarAExcecaoDoDelegate() {
		var input = umLoginValido();
		when(autenticar.executar(input)).thenThrow(new CredenciaisInvalidasException());

		assertThrows(
				CredenciaisInvalidasException.class, () -> new LoggingAutenticarPacienteUseCase(autenticar).executar(input));
		verify(autenticar).executar(input);
	}
}
