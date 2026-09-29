package com.dosealerta.notificacao.infra.decorator;

import static com.dosealerta.notificacao.NotificacaoFixtures.umaSolicitacaoValida;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.dosealerta.notificacao.TesteUnitarioBase;
import com.dosealerta.notificacao.core.dto.SolicitarEnvioInput;
import com.dosealerta.notificacao.core.exception.TelefoneObrigatorioException;
import com.dosealerta.notificacao.core.usecase.PublicarEventosPendentesUseCase;
import com.dosealerta.notificacao.core.usecase.SolicitarEnvioUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class LoggingUseCasesTest extends TesteUnitarioBase {

	@Mock
	private SolicitarEnvioUseCase solicitar;

	@Mock
	private PublicarEventosPendentesUseCase publicar;

	private SolicitarEnvioInput input;

	@BeforeEach
	void setUp() {
		input = umaSolicitacaoValida();
	}

	@Test
	void deveDelegarASolicitacao() {
		new LoggingSolicitarEnvioUseCase(solicitar).executar(input);

		verify(solicitar).executar(input);
	}

	@Test
	void deveDelegarAPublicacao() {
		new LoggingPublicarEventosPendentesUseCase(publicar).executar();

		verify(publicar).executar();
	}

	@Test
	void devePropagarAExcecaoDoDelegate() {
		doThrow(new TelefoneObrigatorioException()).when(solicitar).executar(input);

		assertThrows(TelefoneObrigatorioException.class, () -> new LoggingSolicitarEnvioUseCase(solicitar).executar(input));
	}
}
