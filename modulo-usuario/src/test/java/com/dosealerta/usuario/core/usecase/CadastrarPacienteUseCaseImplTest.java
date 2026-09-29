package com.dosealerta.usuario.core.usecase;

import static com.dosealerta.usuario.UsuarioFixtures.CLOCK_FIXO;
import static com.dosealerta.usuario.UsuarioFixtures.INSTANTE_FIXO;
import static com.dosealerta.usuario.UsuarioFixtures.NOME;
import static com.dosealerta.usuario.UsuarioFixtures.SENHA;
import static com.dosealerta.usuario.UsuarioFixtures.SENHA_HASH;
import static com.dosealerta.usuario.UsuarioFixtures.TELEFONE;
import static com.dosealerta.usuario.UsuarioFixtures.umCadastroValido;
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
import com.dosealerta.usuario.core.dto.CadastrarPacienteInput;
import com.dosealerta.usuario.core.exception.NomeObrigatorioException;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;
import com.dosealerta.usuario.core.rules.cadastro.CadastroPacienteContext;
import com.dosealerta.usuario.core.rules.cadastro.ValidadorCadastroPacienteRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

class CadastrarPacienteUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private PacienteRepositoryGateway pacienteRepositoryGateway;

	@Mock
	private SenhaGateway senhaGateway;

	@Mock
	private ValidadorCadastroPacienteRule regra;

	@Captor
	private ArgumentCaptor<CadastroPacienteContext> contextCaptor;

	private CadastrarPacienteUseCaseImpl useCase;
	private CadastrarPacienteInput input;

	@BeforeEach
	void setUp() {
		useCase = new CadastrarPacienteUseCaseImpl(pacienteRepositoryGateway, senhaGateway, CLOCK_FIXO, List.of(regra));
		input = umCadastroValido();
	}

	@Test
	void deveCadastrarPacienteComSenhaHasheadaEDataDoRelogio() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(senhaGateway.hash(SENHA)).thenReturn(SENHA_HASH);
		when(pacienteRepositoryGateway.salvar(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

		Paciente resultado = useCase.executar(input);

		assertEquals(NOME, resultado.getNome());
		assertEquals(TELEFONE, resultado.getTelefone());
		assertEquals(SENHA_HASH, resultado.getSenhaHash());
		assertEquals(INSTANTE_FIXO, resultado.getCriadoEm());
	}

	@Test
	void devePassarPeloContextComTelefoneJaCadastradoQuandoExiste() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(umPaciente()));
		doThrow(new NomeObrigatorioException()).when(regra).validar(any());

		assertThrows(NomeObrigatorioException.class, () -> useCase.executar(input));

		verify(regra).validar(contextCaptor.capture());
		assertEquals(true, contextCaptor.getValue().telefoneJaCadastrado());
		assertEquals(input, contextCaptor.getValue().input());
	}

	@Test
	void naoDeveSalvarQuandoAlgumaRegraFalha() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.empty());
		doThrow(new NomeObrigatorioException()).when(regra).validar(any());

		assertThrows(NomeObrigatorioException.class, () -> useCase.executar(input));

		verify(pacienteRepositoryGateway, never()).salvar(any());
	}
}
