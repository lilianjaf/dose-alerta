package com.dosealerta.usuario.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.usuario.core.domain.Paciente;
import com.dosealerta.usuario.core.dto.IdentificarPacienteInput;
import com.dosealerta.usuario.core.dto.IdentificarPacienteOutput;
import com.dosealerta.usuario.core.exception.TelefoneJaCadastradoException;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdentificarPacientePorTelefoneUseCaseTest {

	private static final String TELEFONE = "+5511999999999";

	@Mock
	private PacienteRepositoryGateway pacienteRepositoryGateway;

	@Mock
	private CadastroSusGateway cadastroSusGateway;

	private IdentificarPacientePorTelefoneUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new IdentificarPacientePorTelefoneUseCase(pacienteRepositoryGateway, cadastroSusGateway);
	}

	@Test
	void deveDevolverCompletoQuandoOTelefoneJaTemCadastro() {
		Paciente existente = Paciente.novo("Maria da Silva", TELEFONE, "hash");
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(existente));

		IdentificarPacienteOutput resultado = useCase.executar(new IdentificarPacienteInput(TELEFONE));

		assertEquals("Maria da Silva", resultado.nome());
		assertTrue(resultado.cadastroCompleto());
		assertFalse(resultado.recemCriado());
		verify(cadastroSusGateway, never()).buscarNomePorTelefone(any());
	}

	@Test
	void deveDevolverIncompletoSemConsultarOSusDeNovoQuandoJaTemPlaceholder() {
		Paciente placeholder = Paciente.novo(null, TELEFONE, null);
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.of(placeholder));

		IdentificarPacienteOutput resultado = useCase.executar(new IdentificarPacienteInput(TELEFONE));

		assertNull(resultado.nome());
		assertFalse(resultado.cadastroCompleto());

		assertFalse(resultado.recemCriado());
		verify(cadastroSusGateway, never()).buscarNomePorTelefone(any());
	}

	@Test
	void deveIdentificarDireitoQuandoOSusAchaOTelefone() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(cadastroSusGateway.buscarNomePorTelefone(TELEFONE)).thenReturn(Optional.of("Joana Souza"));
		when(pacienteRepositoryGateway.salvar(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

		IdentificarPacienteOutput resultado = useCase.executar(new IdentificarPacienteInput(TELEFONE));

		assertEquals("Joana Souza", resultado.nome());
		assertTrue(resultado.cadastroCompleto());
	}

	@Test
	void deveCriarCadastroIncompletoMarcadoComoRecemCriadoQuandoOSusNaoAchaOTelefone() {
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(cadastroSusGateway.buscarNomePorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(pacienteRepositoryGateway.salvar(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

		IdentificarPacienteOutput resultado = useCase.executar(new IdentificarPacienteInput(TELEFONE));

		assertNull(resultado.nome());
		assertFalse(resultado.cadastroCompleto());

		assertTrue(resultado.recemCriado());
	}

	@Test
	void deveReconsultarEmVezDePropagarConflitoQuandoDuasMensagensColidem() {
		Paciente jaSalvoPorOutraRequisicao = Paciente.novo(null, TELEFONE, null);
		when(pacienteRepositoryGateway.buscarPorTelefone(TELEFONE))
				.thenReturn(Optional.empty())
				.thenReturn(Optional.of(jaSalvoPorOutraRequisicao));
		when(cadastroSusGateway.buscarNomePorTelefone(TELEFONE)).thenReturn(Optional.empty());
		when(pacienteRepositoryGateway.salvar(any(Paciente.class)))
				.thenThrow(new TelefoneJaCadastradoException(TELEFONE));

		IdentificarPacienteOutput resultado = useCase.executar(new IdentificarPacienteInput(TELEFONE));

		assertFalse(resultado.cadastroCompleto());
	}
}
