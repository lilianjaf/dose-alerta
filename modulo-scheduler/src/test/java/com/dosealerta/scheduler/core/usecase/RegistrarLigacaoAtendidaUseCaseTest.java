package com.dosealerta.scheduler.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import com.dosealerta.scheduler.core.domain.StatusAlarme;
import com.dosealerta.scheduler.core.domain.TipoInteracao;
import com.dosealerta.scheduler.core.exception.AlarmePendenteNaoEncontradoException;
import com.dosealerta.scheduler.core.exception.ConflitoConcorrenciaException;
import com.dosealerta.scheduler.core.gateway.AlarmeRepositoryGateway;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarLigacaoAtendidaUseCaseTest {

	@Mock
	private AlarmeRepositoryGateway alarmeRepositoryGateway;

	private RegistrarLigacaoAtendidaUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RegistrarLigacaoAtendidaUseCase(alarmeRepositoryGateway);
	}

	@Test
	void deveRegistrarInteracaoSemAlterarOStatusDoAlarme() {
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now());
		alarme.registrarEnvio(EtapaEscalonamento.LIGACAO, Instant.now());
		when(alarmeRepositoryGateway.buscarPendenteMaisRecentePorTelefone("+5511999999999"))
				.thenReturn(Optional.of(alarme));
		when(alarmeRepositoryGateway.salvar(any(Alarme.class))).thenAnswer(inv -> inv.getArgument(0));

		Alarme resultado = useCase.executar("+5511999999999");

		assertEquals(StatusAlarme.PENDENTE, resultado.getStatus());
		assertEquals(1, resultado.getInteracoes().size());
		assertEquals(TipoInteracao.LIGACAO_ATENDIDA, resultado.getInteracoes().get(0).tipo());
		verify(alarmeRepositoryGateway).salvar(alarme);
	}

	@Test
	void deveLancarExcecaoQuandoNaoHaAlarmePendenteParaOTelefone() {
		when(alarmeRepositoryGateway.buscarPendenteMaisRecentePorTelefone("+5511999999999"))
				.thenReturn(Optional.empty());

		assertThrows(AlarmePendenteNaoEncontradoException.class, () -> useCase.executar("+5511999999999"));
	}

	@Test
	void deveTentarNovamenteAposConflitoDeConcorrenciaEConseguirRegistrar() {
		when(alarmeRepositoryGateway.buscarPendenteMaisRecentePorTelefone("+5511999999999"))
				.thenAnswer(inv -> Optional.of(alarmePendenteRecemEnviado()));
		when(alarmeRepositoryGateway.salvar(any(Alarme.class)))
				.thenThrow(new ConflitoConcorrenciaException(UUID.randomUUID(), new RuntimeException("conflito")))
				.thenAnswer(inv -> inv.getArgument(0));

		Alarme resultado = useCase.executar("+5511999999999");

		assertEquals(1, resultado.getInteracoes().size());
		verify(alarmeRepositoryGateway, times(2)).salvar(any(Alarme.class));
	}

	@Test
	void deveDesistirAposEsgotarAsTentativas() {
		when(alarmeRepositoryGateway.buscarPendenteMaisRecentePorTelefone("+5511999999999"))
				.thenAnswer(inv -> Optional.of(alarmePendenteRecemEnviado()));
		when(alarmeRepositoryGateway.salvar(any(Alarme.class)))
				.thenThrow(new ConflitoConcorrenciaException(UUID.randomUUID(), new RuntimeException("conflito")));

		assertThrows(ConflitoConcorrenciaException.class, () -> useCase.executar("+5511999999999"));
		verify(alarmeRepositoryGateway, times(3)).salvar(any(Alarme.class));
	}

	private static Alarme alarmePendenteRecemEnviado() {
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", Instant.now());
		alarme.registrarEnvio(EtapaEscalonamento.LIGACAO, Instant.now());
		return alarme;
	}
}
