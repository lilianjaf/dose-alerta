package com.dosealerta.usuario.core.usecase;

import static com.dosealerta.usuario.UsuarioFixtures.SENHA;
import static com.dosealerta.usuario.UsuarioFixtures.SENHA_HASH;
import static com.dosealerta.usuario.UsuarioFixtures.TELEFONE;
import static com.dosealerta.usuario.UsuarioFixtures.umLoginValido;
import static com.dosealerta.usuario.UsuarioFixtures.umPaciente;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.AutenticarPacienteInput;
import com.dosealerta.usuario.core.exception.CredenciaisInvalidasException;
import com.dosealerta.usuario.core.gateway.AutenticacaoGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;
import com.dosealerta.usuario.core.rules.autenticacao.AutenticacaoContext;
import com.dosealerta.usuario.core.rules.autenticacao.ValidadorAutenticacaoRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

class AutenticarPacienteUseCaseImplTest extends TesteUnitarioBase {

	private static final String TOKEN = "token-fake";

	@Mock
	private PacienteRepositoryGateway pacienteRepositoryGateway;

	@Mock
	private SenhaGateway senhaGateway;

	@Mock
	private AutenticacaoGateway autenticacaoGateway;

	@Mock
	private ValidadorAutenticacaoRule regra;

	@Captor
	private ArgumentCaptor<AutenticacaoContext> contextCaptor;

	private AutenticarPacienteUseCaseImpl useCase;
	private AutenticarPacienteInput input;
	private Paciente paciente;

	@BeforeEach
	void setUp() {
		useCase = new AutenticarPacienteUseCaseImpl(
				pacienteRepositoryGateway, senhaGateway, autenticacaoGateway, List.of(regra));
		input = umLoginValido();
		paciente = umPaciente();
	}

	@Test
	void deveEmitirTokenQuandoCredenciaisValidas() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(paciente));
		when(senhaGateway.confere(SENHA, SENHA_HASH)).thenReturn(true);
		when(autenticacaoGateway.emitirToken(paciente)).thenReturn(TOKEN);

		assertEquals(TOKEN, useCase.executar(input).token());
	}

	@Test
	void devePassarPeloContextSenhaNaoConfereQuandoSenhaErrada() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(paciente));
		when(senhaGateway.confere(SENHA, SENHA_HASH)).thenReturn(false);
		doThrow(new CredenciaisInvalidasException()).when(regra).validar(any());

		assertThrows(CredenciaisInvalidasException.class, () -> useCase.executar(input));

		verify(regra).validar(contextCaptor.capture());
		assertEquals(false, contextCaptor.getValue().senhaConfere());
		verify(autenticacaoGateway, never()).emitirToken(any());
	}

	@Test
	void devePassarPeloContextSemPacienteQuandoTelefoneNaoExiste() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.empty());
		doThrow(new CredenciaisInvalidasException()).when(regra).validar(any());

		assertThrows(CredenciaisInvalidasException.class, () -> useCase.executar(input));

		verify(regra).validar(contextCaptor.capture());
		assertEquals(null, contextCaptor.getValue().paciente());
		verify(senhaGateway, never()).confere(any(), any());
	}
}
