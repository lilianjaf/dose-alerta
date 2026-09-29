package com.dosealerta.mensageria.core.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;
import com.dosealerta.mensageria.core.dto.IdentificarPacienteResultado;
import com.dosealerta.mensageria.core.dto.IntencaoAudio;
import com.dosealerta.mensageria.core.dto.InterpretacaoAudioResultado;
import com.dosealerta.mensageria.core.dto.ReceitaCriada;
import com.dosealerta.mensageria.core.dto.ReceitaExtraidaResultado;
import com.dosealerta.mensageria.core.exception.DadosReceitaIncompletosException;
import com.dosealerta.mensageria.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.mensageria.core.exception.ReceitaPendenteNaoEncontradaException;
import com.dosealerta.mensageria.core.gateway.MediaDownloadGateway;
import com.dosealerta.mensageria.core.gateway.MensageriaGateway;
import com.dosealerta.mensageria.core.gateway.PacienteClientGateway;
import com.dosealerta.mensageria.core.gateway.ReceitaClientGateway;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessarMensagemRecebidaUseCaseTest {

	private static final String TELEFONE = "+5511999999999";
	private static final UUID PACIENTE_ID = UUID.randomUUID();

	@Mock
	private PacienteClientGateway pacienteClientGateway;

	@Mock
	private ReceitaClientGateway receitaClientGateway;

	@Mock
	private MediaDownloadGateway mediaDownloadGateway;

	@Mock
	private MensageriaGateway mensageriaGateway;

	@Mock
	private ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase;

	private ProcessarMensagemRecebidaUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ProcessarMensagemRecebidaUseCase(
				pacienteClientGateway,
				receitaClientGateway,
				mediaDownloadGateway,
				mensageriaGateway,
				processarRespostaMensagemUseCase);
	}

	private IdentificarPacienteResultado completo() {
		return new IdentificarPacienteResultado(PACIENTE_ID, "Maria da Silva", true, false);
	}

	private DadosMensagemRecebida texto(String corpo) {
		return new DadosMensagemRecebida(TELEFONE, corpo, null, null, 0, null);
	}

	private DadosMensagemRecebida audio() {
		return new DadosMensagemRecebida(TELEFONE, null, null, "https://twilio/media/1", 1, "audio/ogg");
	}

	private String ultimaMensagemEnviada() {
		ArgumentCaptor<ConteudoMensagem> captor = ArgumentCaptor.forClass(ConteudoMensagem.class);
		verify(mensageriaGateway).enviarMensagem(eq(new ContatoWhatsApp(TELEFONE)), captor.capture());
		return ((ConteudoMensagem.Texto) captor.getValue()).texto();
	}

	@Test
	void devePedirONumeroDeInscricaoSusQuandoOCadastroEstaIncompletoESemTexto() {
		when(pacienteClientGateway.identificar(TELEFONE))
				.thenReturn(new IdentificarPacienteResultado(PACIENTE_ID, null, false, true));

		useCase.executar(new DadosMensagemRecebida(TELEFONE, null, null, null, 0, null));

		verify(pacienteClientGateway, never()).completarCadastro(any(), any());
		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("inscrição"));
	}

	@Test
	void devePedirONumeroDeInscricaoSusSemTratarOPrimeiroContatoComoRespostaMesmoQuandoTemTexto() {

		when(pacienteClientGateway.identificar(TELEFONE))
				.thenReturn(new IdentificarPacienteResultado(PACIENTE_ID, null, false, true));

		useCase.executar(texto("oi"));

		verify(pacienteClientGateway, never()).completarCadastro(any(), any());
		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("inscrição"));
	}

	@Test
	void deveCompletarOCadastroQuandoRespondeONumeroDeInscricaoSusEMandarBoasVindasSemMencionarNome() {
		when(pacienteClientGateway.identificar(TELEFONE))
				.thenReturn(new IdentificarPacienteResultado(PACIENTE_ID, null, false, false));

		useCase.executar(texto("700000000000001"));

		verify(pacienteClientGateway).completarCadastro(TELEFONE, "700000000000001");
		assertEquals(true, ultimaMensagemEnviada().contains("SusIA"));
		verifyNoInteractions(receitaClientGateway);
	}

	@Test
	void devePedirONumeroDeInscricaoSusNovamenteQuandoOInformadoNaoForEncontrado() {
		when(pacienteClientGateway.identificar(TELEFONE))
				.thenReturn(new IdentificarPacienteResultado(PACIENTE_ID, null, false, false));
		doThrow(new NumeroInscricaoSusNaoEncontradoException("000000000000000"))
				.when(pacienteClientGateway)
				.completarCadastro(TELEFONE, "000000000000000");

		useCase.executar(texto("000000000000000"));

		String mensagem = ultimaMensagemEnviada().toLowerCase();
		assertEquals(true, mensagem.contains("sus"));
		assertEquals(false, mensagem.contains("cadastro confirmado"));
	}

	@Test
	void deveExtrairAReceitaQuandoChegaUmaFotoDeUmPacienteJaIdentificado() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(mediaDownloadGateway.baixar(URI.create("https://twilio/media/1"))).thenReturn(new byte[] {1, 2, 3});
		when(receitaClientGateway.extrair(eq(PACIENTE_ID), eq(TELEFONE), any(), any(), eq("image/jpeg")))
				.thenReturn(new ReceitaExtraidaResultado(
						List.of(new ReceitaCriada("Losartana", "50mg", 24, 30, List.of())), List.of()));

		useCase.executar(new DadosMensagemRecebida(TELEFONE, null, null, "https://twilio/media/1", 1, "image/jpeg"));

		String mensagem = ultimaMensagemEnviada();
		assertEquals(true, mensagem.contains("CONFIRMAR"));
		assertEquals(true, mensagem.contains("Losartana"));
		assertEquals(true, mensagem.contains("50mg"));
	}

	@Test
	void deveMandarMensagemDeAjudaQuandoNaoTemFotoNemTexto() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());

		useCase.executar(new DadosMensagemRecebida(TELEFONE, null, null, null, 0, null));

		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("foto"));
		verifyNoInteractions(receitaClientGateway);
	}

	@Test
	void deveConfirmarSemCorrecaoQuandoOTextoNaoCasaComOFormatoDePendencias() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(receitaClientGateway.confirmarPorTelefone(TELEFONE, CorrecaoReceita.vazia())).thenReturn("Losartana");

		useCase.executar(texto("CONFIRMAR"));

		assertEquals(true, ultimaMensagemEnviada().contains("Losartana"));
	}

	@Test
	void deveConfirmarComACorrecaoQuandoOTextoCasaComOFormatoDePendencias() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(receitaClientGateway.confirmarPorTelefone(TELEFONE, new CorrecaoReceita(null, "1 comprimido", 8, 7)))
				.thenReturn("Amoxicilina");

		useCase.executar(texto("1 comprimido; 8; 7"));

		verify(receitaClientGateway).confirmarPorTelefone(TELEFONE, new CorrecaoReceita(null, "1 comprimido", 8, 7));
		assertEquals(true, ultimaMensagemEnviada().contains("Amoxicilina"));
	}

	@Test
	void naoDeveConfirmarComDadosErradosQuandoOTextoNaoEReconhecido() {

		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());

		useCase.executar(texto("não, a dose está errada"));

		verifyNoInteractions(receitaClientGateway);
		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("não entendi"));
	}

	@Test
	void deveCairNaConfirmacaoDeRotinaQuandoNaoHaReceitaPendente() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(receitaClientGateway.confirmarPorTelefone(TELEFONE, CorrecaoReceita.vazia()))
				.thenThrow(new ReceitaPendenteNaoEncontradaException(TELEFONE));

		useCase.executar(texto("CONFIRMAR"));

		verify(processarRespostaMensagemUseCase, times(1)).executar(TELEFONE, "CONFIRMAR", null);
		verifyNoInteractions(mensageriaGateway);
	}

	@Test
	void deveAvisarQuandoTomeiConfirmaUmAlarmeDeRotinaComSucesso() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(processarRespostaMensagemUseCase.executar(TELEFONE, "TOMEI", null)).thenReturn(true);

		useCase.executar(texto("TOMEI"));

		verifyNoInteractions(receitaClientGateway);
		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("registrei"));
	}

	@Test
	void naoDeveAvisarQuandoTomeiNaoEncontraNenhumAlarmeParaConfirmar() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(processarRespostaMensagemUseCase.executar(TELEFONE, "TOMEI", null)).thenReturn(false);

		useCase.executar(texto("TOMEI"));

		verifyNoInteractions(receitaClientGateway);
		verifyNoInteractions(mensageriaGateway);
	}

	@Test
	void deveReconhecerNaoTomeiSemTentarConfirmarNadaEApenasAvisar() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());

		useCase.executar(texto("NÃO TOMEI"));

		verifyNoInteractions(receitaClientGateway);
		verifyNoInteractions(processarRespostaMensagemUseCase);
		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("lembrar"));
	}

	@Test
	void devePedirOsCamposPendentesQuandoAConfirmacaoAindaFaltaDados() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(receitaClientGateway.confirmarPorTelefone(TELEFONE, CorrecaoReceita.vazia()))
				.thenThrow(new DadosReceitaIncompletosException(List.of("dose", "duracaoDias")));

		useCase.executar(texto("CONFIRMAR"));

		String mensagem = ultimaMensagemEnviada();
		assertEquals(true, mensagem.contains("dose"));
		assertEquals(true, mensagem.contains(";"));
	}

	@Test
	void deveConfirmarADoseQuandoOAudioEClassificadoComoTomei() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(mediaDownloadGateway.baixar(URI.create("https://twilio/media/1"))).thenReturn(new byte[] {1, 2, 3});
		when(receitaClientGateway.interpretarAudio(any(), eq("audio/ogg")))
				.thenReturn(new InterpretacaoAudioResultado(IntencaoAudio.TOMEI, null, null, null));
		when(processarRespostaMensagemUseCase.executar(TELEFONE, "TOMEI", null)).thenReturn(true);

		useCase.executar(audio());

		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("registrei"));
	}

	@Test
	void deveAvisarQueAindaNaoTomouQuandoOAudioEClassificadoComoNaoTomei() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(mediaDownloadGateway.baixar(URI.create("https://twilio/media/1"))).thenReturn(new byte[] {1, 2, 3});
		when(receitaClientGateway.interpretarAudio(any(), eq("audio/ogg")))
				.thenReturn(new InterpretacaoAudioResultado(IntencaoAudio.NAO_TOMEI, null, null, null));

		useCase.executar(audio());

		verify(processarRespostaMensagemUseCase, never()).executar(any(), any(), any());
		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("lembrar"));
	}

	@Test
	void deveConfirmarAReceitaQuandoOAudioEClassificadoComoConfirmar() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(mediaDownloadGateway.baixar(URI.create("https://twilio/media/1"))).thenReturn(new byte[] {1, 2, 3});
		when(receitaClientGateway.interpretarAudio(any(), eq("audio/ogg")))
				.thenReturn(new InterpretacaoAudioResultado(IntencaoAudio.CONFIRMAR, null, null, null));
		when(receitaClientGateway.confirmarPorTelefone(TELEFONE, CorrecaoReceita.vazia())).thenReturn("Losartana");

		useCase.executar(audio());

		assertEquals(true, ultimaMensagemEnviada().contains("Losartana"));
	}

	@Test
	void deveConfirmarComACorrecaoExtraidaDoAudioQuandoClassificadoComoCorrecao() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(mediaDownloadGateway.baixar(URI.create("https://twilio/media/1"))).thenReturn(new byte[] {1, 2, 3});
		when(receitaClientGateway.interpretarAudio(any(), eq("audio/ogg")))
				.thenReturn(new InterpretacaoAudioResultado(IntencaoAudio.CORRECAO, "1 comprimido", 8, 7));
		when(receitaClientGateway.confirmarPorTelefone(TELEFONE, new CorrecaoReceita(null, "1 comprimido", 8, 7)))
				.thenReturn("Amoxicilina");

		useCase.executar(audio());

		assertEquals(true, ultimaMensagemEnviada().contains("Amoxicilina"));
	}

	@Test
	void deveResponderNaoEntendiQuandoOAudioNaoEClassificado() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(mediaDownloadGateway.baixar(URI.create("https://twilio/media/1"))).thenReturn(new byte[] {1, 2, 3});
		when(receitaClientGateway.interpretarAudio(any(), eq("audio/ogg"))).thenReturn(InterpretacaoAudioResultado.naoEntendido());

		useCase.executar(audio());

		verifyNoInteractions(processarRespostaMensagemUseCase);
		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("não entendi"));
	}

	@Test
	void naoDeveDeixarUmaFalhaInesperadaEscaparDoUseCase() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenThrow(new RuntimeException("indisponível"));

		useCase.executar(texto("oi"));

		verifyNoInteractions(mensageriaGateway);
	}
}
