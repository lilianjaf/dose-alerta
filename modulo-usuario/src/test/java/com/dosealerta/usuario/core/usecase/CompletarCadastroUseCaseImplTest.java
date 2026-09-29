package com.dosealerta.usuario.core.usecase;

import static com.dosealerta.usuario.UsuarioFixtures.NOME_NO_SUS;
import static com.dosealerta.usuario.UsuarioFixtures.NUMERO_INSCRICAO_SUS;
import static com.dosealerta.usuario.UsuarioFixtures.TELEFONE;
import static com.dosealerta.usuario.UsuarioFixtures.umCompletarCadastroValido;
import static com.dosealerta.usuario.UsuarioFixtures.umPacienteIncompleto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.usuario.core.exception.PacienteNaoEncontradoException;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.rules.completarcadastro.ValidadorCompletarCadastroRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class CompletarCadastroUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private PacienteRepositoryGateway pacienteRepositoryGateway;

	@Mock
	private CadastroSusGateway cadastroSusGateway;

	@Mock
	private ValidadorCompletarCadastroRule regra;

	private CompletarCadastroUseCaseImpl useCase;
	private CompletarCadastroInput input;
	private Paciente placeholder;

	@BeforeEach
	void setUp() {
		useCase = new CompletarCadastroUseCaseImpl(pacienteRepositoryGateway, cadastroSusGateway, List.of(regra));
		input = umCompletarCadastroValido();
		placeholder = umPacienteIncompleto();
	}

	@Test
	void devePreencherONomeResolvidoPeloSusMantendoIdETelefone() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(placeholder));
		when(cadastroSusGateway.buscarNomePorNumeroInscricao(NUMERO_INSCRICAO_SUS))
				.thenReturn(Optional.of(NOME_NO_SUS));
		when(pacienteRepositoryGateway.salvar(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

		Paciente resultado = useCase.executar(input);

		assertEquals(NOME_NO_SUS, resultado.getNome());
		assertEquals(placeholder.getId(), resultado.getId());
		assertEquals(TELEFONE, resultado.getTelefone());
	}

	@Test
	void naoDeveSalvarQuandoTelefoneNaoTemCadastro() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.empty());
		doThrow(new PacienteNaoEncontradoException(TELEFONE)).when(regra).validar(any());

		assertThrows(PacienteNaoEncontradoException.class, () -> useCase.executar(input));

		verify(pacienteRepositoryGateway, never()).salvar(any());
	}

	@Test
	void naoDeveSalvarQuandoNumeroDeInscricaoNaoExisteNoSus() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(placeholder));
		when(cadastroSusGateway.buscarNomePorNumeroInscricao(NUMERO_INSCRICAO_SUS)).thenReturn(Optional.empty());
		doThrow(new NumeroInscricaoSusNaoEncontradoException(NUMERO_INSCRICAO_SUS)).when(regra).validar(any());

		assertThrows(NumeroInscricaoSusNaoEncontradoException.class, () -> useCase.executar(input));

		verify(pacienteRepositoryGateway, never()).salvar(any());
	}
}
