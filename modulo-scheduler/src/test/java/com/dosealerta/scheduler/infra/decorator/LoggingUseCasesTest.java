package com.dosealerta.scheduler.infra.decorator;

import static com.dosealerta.scheduler.SchedulerFixtures.ALARME_ID;
import static com.dosealerta.scheduler.SchedulerFixtures.TELEFONE;
import static com.dosealerta.scheduler.SchedulerFixtures.umAlarme;
import static com.dosealerta.scheduler.SchedulerFixtures.umaCriacaoValida;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.scheduler.TesteUnitarioBase;
import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.dto.CriarAlarmeInput;
import com.dosealerta.scheduler.core.dto.ResultadoCriarAlarme;
import com.dosealerta.scheduler.core.exception.AlarmeNaoEncontradoException;
import com.dosealerta.scheduler.core.usecase.BuscarAlarmeUseCase;
import com.dosealerta.scheduler.core.usecase.CriarAlarmeUseCase;
import com.dosealerta.scheduler.core.usecase.EscalonarAlarmesUseCase;
import com.dosealerta.scheduler.core.usecase.PublicarEventosInteracaoPendentesUseCase;
import com.dosealerta.scheduler.core.usecase.PublicarEventosPendentesUseCase;
import com.dosealerta.scheduler.core.usecase.RegistrarConfirmacaoUseCase;
import com.dosealerta.scheduler.core.usecase.RegistrarLigacaoAtendidaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class LoggingUseCasesTest extends TesteUnitarioBase {

	@Mock
	private BuscarAlarmeUseCase buscar;

	@Mock
	private CriarAlarmeUseCase criar;

	@Mock
	private RegistrarConfirmacaoUseCase confirmar;

	@Mock
	private RegistrarLigacaoAtendidaUseCase ligacao;

	@Mock
	private EscalonarAlarmesUseCase escalonar;

	@Mock
	private PublicarEventosPendentesUseCase publicar;

	@Mock
	private PublicarEventosInteracaoPendentesUseCase publicarInteracao;

	private Alarme alarme;
	private CriarAlarmeInput criacao;

	@BeforeEach
	void setUp() {
		alarme = umAlarme();
		criacao = umaCriacaoValida();
	}

	@Test
	void deveDelegarEBuscar() {
		when(buscar.executar(ALARME_ID)).thenReturn(alarme);

		assertSame(alarme, new LoggingBuscarAlarmeUseCase(buscar).executar(ALARME_ID));
	}

	@Test
	void deveDelegarECriar() {
		var resultado = new ResultadoCriarAlarme(alarme, false);
		when(criar.executar(criacao)).thenReturn(resultado);

		assertSame(resultado, new LoggingCriarAlarmeUseCase(criar).executar(criacao));
	}

	@Test
	void deveDelegarEConfirmar() {
		when(confirmar.executar(TELEFONE)).thenReturn(alarme);

		assertSame(alarme, new LoggingRegistrarConfirmacaoUseCase(confirmar).executar(TELEFONE));
	}

	@Test
	void deveDelegarERegistrarLigacao() {
		when(ligacao.executar(TELEFONE)).thenReturn(alarme);

		assertSame(alarme, new LoggingRegistrarLigacaoAtendidaUseCase(ligacao).executar(TELEFONE));
	}

	@Test
	void deveDelegarOsUseCasesSemRetorno() {
		new LoggingEscalonarAlarmesUseCase(escalonar).executar();
		new LoggingPublicarEventosPendentesUseCase(publicar).executar();
		new LoggingPublicarEventosInteracaoPendentesUseCase(publicarInteracao).executar();

		verify(escalonar).executar();
		verify(publicar).executar();
		verify(publicarInteracao).executar();
	}

	@Test
	void devePropagarAExcecaoDoDelegate() {
		when(buscar.executar(ALARME_ID)).thenThrow(new AlarmeNaoEncontradoException(ALARME_ID));

		assertThrows(
				AlarmeNaoEncontradoException.class, () -> new LoggingBuscarAlarmeUseCase(buscar).executar(ALARME_ID));
	}
}
