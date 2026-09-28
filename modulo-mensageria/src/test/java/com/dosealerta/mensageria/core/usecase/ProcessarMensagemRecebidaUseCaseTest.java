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
		// Bug já visto ao vivo: telefone nunca visto manda "oi" como primeira mensagem — "oi" não pode virar
		// número de inscrição.
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
		assertEquals(true, ultimaMensagemEnviada().toLowerCase().contains("cadastro"));
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
						List.of(new ReceitaCriada("Losartana", List.of())), List.of()));

		useCase.executar(new DadosMensagemRecebida(TELEFONE, null, null, "https://twilio/media/1", 1, "image/jpeg"));

		assertEquals(true, ultimaMensagemEnviada().contains("CONFIRMAR"));
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
	void deveCairNaConfirmacaoDeRotinaQuandoNaoHaReceitaPendente() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenReturn(completo());
		when(receitaClientGateway.confirmarPorTelefone(TELEFONE, CorrecaoReceita.vazia()))
				.thenThrow(new ReceitaPendenteNaoEncontradaException(TELEFONE));

		useCase.executar(texto("CONFIRMAR"));

		verify(processarRespostaMensagemUseCase, times(1)).executar(TELEFONE, "CONFIRMAR", null);
		verifyNoInteractions(mensageriaGateway);
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
	void naoDeveDeixarUmaFalhaInesperadaEscaparDoUseCase() {
		when(pacienteClientGateway.identificar(TELEFONE)).thenThrow(new RuntimeException("indisponível"));

		useCase.executar(texto("oi"));

		verifyNoInteractions(mensageriaGateway);
	}
}
