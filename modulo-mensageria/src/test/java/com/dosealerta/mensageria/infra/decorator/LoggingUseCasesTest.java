package com.dosealerta.mensageria.infra.decorator;

import static com.dosealerta.mensageria.MensageriaFixtures.TELEFONE;
import static com.dosealerta.mensageria.MensageriaFixtures.umaMensagemDeTexto;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dosealerta.mensageria.TesteUnitarioBase;
import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;
import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarMensagemRecebidaUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarRespostaMensagemUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarStatusLigacaoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class LoggingUseCasesTest extends TesteUnitarioBase {

	private static final String CORPO = "confirmar";
	private static final String DIGITO = "1";
	private static final String STATUS = "in-progress";

	@Mock
	private ProcessarRespostaMensagemUseCase resposta;

	@Mock
	private ProcessarConfirmacaoLigacaoUseCase confirmacao;

	@Mock
	private ProcessarStatusLigacaoUseCase status;

	@Mock
	private ProcessarMensagemRecebidaUseCase recebida;

	private DadosMensagemRecebida dados;

	@BeforeEach
	void setUp() {
		dados = umaMensagemDeTexto(CORPO);
	}

	@Test
	void deveDelegarEDevolverOResultadoDaResposta() {
		when(resposta.executar(TELEFONE, CORPO, null)).thenReturn(true);

		assertTrue(new LoggingProcessarRespostaMensagemUseCase(resposta).executar(TELEFONE, CORPO, null));
	}

	@Test
	void deveDelegarEDevolverOResultadoDaConfirmacaoDeLigacao() {
		when(confirmacao.executar(TELEFONE, DIGITO)).thenReturn(false);

		assertFalse(new LoggingProcessarConfirmacaoLigacaoUseCase(confirmacao).executar(TELEFONE, DIGITO));
	}

	@Test
	void deveDelegarOStatusEAMensagemRecebida() {
		new LoggingProcessarStatusLigacaoUseCase(status).executar(TELEFONE, STATUS);
		new LoggingProcessarMensagemRecebidaUseCase(recebida).executar(dados);

		verify(status).executar(TELEFONE, STATUS);
		verify(recebida).executar(dados);
	}

	@Test
	void devePropagarAExcecaoDoDelegate() {
		doThrow(new IllegalStateException("falha")).when(recebida).executar(dados);

		assertThrows(IllegalStateException.class, () -> new LoggingProcessarMensagemRecebidaUseCase(recebida).executar(dados));
	}
}
