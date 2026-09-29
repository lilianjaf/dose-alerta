package com.dosealerta.usuario.core.usecase;

import static com.dosealerta.usuario.UsuarioFixtures.CLOCK_FIXO;
import static com.dosealerta.usuario.UsuarioFixtures.NOME;
import static com.dosealerta.usuario.UsuarioFixtures.TELEFONE;
import static com.dosealerta.usuario.UsuarioFixtures.umPaciente;
import static com.dosealerta.usuario.UsuarioFixtures.umPacienteIncompleto;
import static com.dosealerta.usuario.UsuarioFixtures.umaIdentificacaoValida;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.usuario.TesteUnitarioBase;
import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteOutput;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.exception.TelefoneObrigatorioException;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.rules.identificacao.ValidadorIdentificacaoPacienteRule;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class IdentificarPacientePorTelefoneUseCaseImplTest extends TesteUnitarioBase {

	@Mock
	private PacienteRepositoryGateway pacienteRepositoryGateway;

	@Mock
	private CadastroSusGateway cadastroSusGateway;

	@Mock
	private ValidadorIdentificacaoPacienteRule regra;

	private IdentificarPacientePorTelefoneUseCaseImpl useCase;
	private IdentificarPacienteInput input;

	@BeforeEach
	void setUp() {
		useCase = new IdentificarPacientePorTelefoneUseCaseImpl(
				pacienteRepositoryGateway, cadastroSusGateway, CLOCK_FIXO, List.of(regra));
		input = umaIdentificacaoValida();
	}

	@Test
	void deveDevolverCompletoQuandoOTelefoneJaTemCadastro() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(umPaciente()));

		IdentificarPacienteOutput resultado = useCase.executar(input);

		assertEquals(NOME, resultado.nome());
		assertTrue(resultado.cadastroCompleto());
		assertFalse(resultado.recemCriado());
		verify(cadastroSusGateway, never()).buscarNomePorTelefone(any());
	}

	@Test
	void deveDevolverIncompletoSemConsultarOSusQuandoJaTemPlaceholder() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(umPacienteIncompleto()));

		IdentificarPacienteOutput resultado = useCase.executar(input);

		assertNull(resultado.nome());
		assertFalse(resultado.cadastroCompleto());
		assertFalse(resultado.recemCriado());
		verify(cadastroSusGateway, never()).buscarNomePorTelefone(any());
	}

	@Test
	void deveIdentificarDireitoQuandoOSusAchaOTelefone() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(cadastroSusGateway.buscarNomePorTelefone(TELEFONE)).thenReturn(Optional.of(NOME));
		when(pacienteRepositoryGateway.salvar(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

		IdentificarPacienteOutput resultado = useCase.executar(input);

		assertEquals(NOME, resultado.nome());
		assertTrue(resultado.cadastroCompleto());
	}

	@Test
	void deveCriarCadastroIncompletoMarcadoComoRecemCriadoQuandoOSusNaoAchaOTelefone() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(cadastroSusGateway.buscarNomePorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(pacienteRepositoryGateway.salvar(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

		IdentificarPacienteOutput resultado = useCase.executar(input);

		assertNull(resultado.nome());
		assertFalse(resultado.cadastroCompleto());
		assertTrue(resultado.recemCriado());
	}

	@Test
	void deveReconsultarEmVezDePropagarConflitoQuandoDuasMensagensColidem() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE))
				.thenReturn(Optional.empty())
				.thenReturn(Optional.of(umPacienteIncompleto()));
		when(cadastroSusGateway.buscarNomePorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(pacienteRepositoryGateway.salvar(any(Paciente.class)))
				.thenThrow(new TelefoneJaCadastradoException(TELEFONE));

		assertFalse(useCase.executar(input).cadastroCompleto());
	}

	@Test
	void naoDeveConsultarNadaQuandoAlgumaRegraFalha() {
		doThrow(new TelefoneObrigatorioException()).when(regra).validar(any());

		assertThrows(TelefoneObrigatorioException.class, () -> useCase.executar(input));

		verify(pacienteRepositoryGateway, never()).buscarPorTelefone(any());
	}
}
