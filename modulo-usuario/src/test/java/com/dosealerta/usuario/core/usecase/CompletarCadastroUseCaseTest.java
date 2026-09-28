package com.dosealerta.usuario.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.CompletarCadastroInput;
import com.dosealerta.usuario.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.usuario.core.exception.PacienteNaoEncontradoException;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompletarCadastroUseCaseTest {

	private static final String TELEFONE = "+5511999999999";
	private static final String NUMERO_INSCRICAO_SUS = "700000000000001";

	@Mock
	private PacienteRepositoryGateway pacienteRepositoryGateway;

	@Mock
	private CadastroSusGateway cadastroSusGateway;

	private CompletarCadastroUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new CompletarCadastroUseCase(pacienteRepositoryGateway, cadastroSusGateway);
	}

	@Test
	void devePreencherONomeResolvidoPeloSusMantendoIdETelefone() {
		Paciente placeholder = Paciente.novo(null, TELEFONE, null);
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(placeholder));
		when(cadastroSusGateway.buscarNomePorNumeroInscricao(NUMERO_INSCRICAO_SUS))
				.thenReturn(Optional.of("Pedro Alves"));
		when(pacienteRepositoryGateway.salvar(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

		Paciente resultado = useCase.executar(new CompletarCadastroInput(TELEFONE, NUMERO_INSCRICAO_SUS));

		assertEquals("Pedro Alves", resultado.getNome());
		assertEquals(placeholder.getId(), resultado.getId());
		assertEquals(TELEFONE, resultado.getTelefone());
	}

	@Test
	void deveLancarExcecaoQuandoTelefoneNaoTemCadastro() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.empty());

		assertThrows(
				PacienteNaoEncontradoException.class,
				() -> useCase.executar(new CompletarCadastroInput(TELEFONE, NUMERO_INSCRICAO_SUS)));
	}

	@Test
	void deveLancarExcecaoQuandoNumeroDeInscricaoNaoEExistente() {
		Paciente placeholder = Paciente.novo(null, TELEFONE, null);
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(placeholder));
		when(cadastroSusGateway.buscarNomePorNumeroInscricao(NUMERO_INSCRICAO_SUS)).thenReturn(Optional.empty());

		assertThrows(
				NumeroInscricaoSusNaoEncontradoException.class,
				() -> useCase.executar(new CompletarCadastroInput(TELEFONE, NUMERO_INSCRICAO_SUS)));
	}
}
