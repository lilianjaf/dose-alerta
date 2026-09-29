package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;
import com.dosealerta.mensageria.core.dto.IdentificarPacienteResultado;
import com.dosealerta.mensageria.core.dto.InterpretacaoAudioResultado;
import com.dosealerta.mensageria.core.dto.ReceitaExtraidaResultado;
import com.dosealerta.mensageria.core.exception.DadosReceitaIncompletosException;
import com.dosealerta.mensageria.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.mensageria.core.exception.ReceitaPendenteNaoEncontradaException;
import com.dosealerta.mensageria.core.gateway.LogGateway;
import com.dosealerta.mensageria.core.gateway.MediaDownloadGateway;
import com.dosealerta.mensageria.core.gateway.MensageriaGateway;
import com.dosealerta.mensageria.core.gateway.PacienteClientGateway;
import com.dosealerta.mensageria.core.gateway.ReceitaClientGateway;
import com.dosealerta.mensageria.core.rules.RegraMensagemReceita;
import com.dosealerta.mensageria.core.rules.RegraParseCorrecaoReceita;
import com.dosealerta.mensageria.core.rules.RegraRespostaPaciente;
import com.dosealerta.mensageria.core.rules.mensagemrecebida.MensagemRecebidaContext;
import com.dosealerta.mensageria.core.rules.mensagemrecebida.ValidadorMensagemRecebidaRule;
import java.net.URI;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

public class ProcessarMensagemRecebidaUseCaseImpl implements ProcessarMensagemRecebidaUseCase {

	private static final String MENSAGEM_FALHA_PROCESSAR = "Falha ao processar mensagem recebida de {}";
	private static final String TEXTO_TOMEI = "TOMEI";
	private static final String TEXTO_CONFIRMAR = "CONFIRMAR";
	private static final String TEXTO_VAZIO = "";

	private final PacienteClientGateway pacienteClientGateway;
	private final ReceitaClientGateway receitaClientGateway;
	private final MediaDownloadGateway mediaDownloadGateway;
	private final MensageriaGateway mensageriaGateway;
	private final ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase;
	private final LogGateway logGateway;
	private final Clock clock;
	private final List<ValidadorMensagemRecebidaRule> rules;

	public ProcessarMensagemRecebidaUseCaseImpl(PacienteClientGateway pacienteClientGateway, ReceitaClientGateway receitaClientGateway, MediaDownloadGateway mediaDownloadGateway, MensageriaGateway mensageriaGateway, ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase, LogGateway logGateway, Clock clock, List<ValidadorMensagemRecebidaRule> rules) {
		this.pacienteClientGateway = pacienteClientGateway;
		this.receitaClientGateway = receitaClientGateway;
		this.mediaDownloadGateway = mediaDownloadGateway;
		this.mensageriaGateway = mensageriaGateway;
		this.processarRespostaMensagemUseCase = processarRespostaMensagemUseCase;
		this.logGateway = logGateway;
		this.clock = clock;
		this.rules = rules;
	}

	@Override
	public void executar(DadosMensagemRecebida dados) {
		try {
			MensagemRecebidaContext context = new MensagemRecebidaContext(dados);
			rules.forEach(rule -> rule.validar(context));
			processar(dados);
		} catch (RuntimeException e) {
			logGateway.aviso(MENSAGEM_FALHA_PROCESSAR, dados.telefone(), e);
		}
	}

	private void processar(DadosMensagemRecebida dados) {
		IdentificarPacienteResultado identificacao = pacienteClientGateway.identificar(dados.telefone());
		if (!identificacao.cadastroCompleto()) {
			identificarOuCompletarCadastro(dados, identificacao);
		} else if (dados.temFoto()) {
			extrairReceita(dados, identificacao);
		} else if (!dados.temTexto() && !dados.temAudio()) {
			enviar(dados.telefone(), RegraMensagemReceita.ajuda());
		} else if (dados.temAudio()) {
			interpretarAudioEAgir(dados);
		} else {
			confirmarOuCairNaRotina(dados);
		}
	}

	private void identificarOuCompletarCadastro(DadosMensagemRecebida dados, IdentificarPacienteResultado identificacao) {
		if (!identificacao.recemCriado() && dados.temTexto()) {
			completarCadastro(dados);
		} else {
			enviar(dados.telefone(), RegraMensagemReceita.pedirNumeroInscricaoSus());
		}
	}

	private void completarCadastro(DadosMensagemRecebida dados) {
		try {
			pacienteClientGateway.completarCadastro(dados.telefone(), dados.corpoOuBotao());
			enviar(dados.telefone(), RegraMensagemReceita.boasVindas());
		} catch (NumeroInscricaoSusNaoEncontradoException e) {
			enviar(dados.telefone(), RegraMensagemReceita.numeroInscricaoSusNaoEncontrado());
		}
	}

	private void extrairReceita(DadosMensagemRecebida dados, IdentificarPacienteResultado identificacao) {
		byte[] imagem = mediaDownloadGateway.baixar(URI.create(dados.mediaUrl0()));
		ReceitaExtraidaResultado resultado = receitaClientGateway.extrair(
				identificacao.pacienteId(), dados.telefone(), clock.instant(), imagem, dados.mediaContentType0());
		enviar(dados.telefone(), RegraMensagemReceita.resumoExtracao(resultado.receitas(), resultado.naoProcessados()));
	}

	private void confirmarOuCairNaRotina(DadosMensagemRecebida dados) {
		if (RegraRespostaPaciente.ehNegacao(dados.corpo(), dados.textoBotao())) {
			enviar(dados.telefone(), RegraMensagemReceita.primeiraDoseAindaNaoTomada());
		} else if (RegraRespostaPaciente.ehConfirmacaoDeDose(dados.corpo(), dados.textoBotao())) {
			confirmarDose(dados.telefone(), dados.corpo(), dados.textoBotao());
		} else {
			corrigirOuConfirmarReceita(dados);
		}
	}

	private void interpretarAudioEAgir(DadosMensagemRecebida dados) {
		byte[] audio = mediaDownloadGateway.baixar(URI.create(dados.mediaUrl0()));
		InterpretacaoAudioResultado interpretacao =
				receitaClientGateway.interpretarAudio(audio, dados.mediaContentType0());
		switch (interpretacao.intencao()) {
			case TOMEI -> confirmarDose(dados.telefone(), TEXTO_TOMEI, null);
			case NAO_TOMEI -> enviar(dados.telefone(), RegraMensagemReceita.primeiraDoseAindaNaoTomada());
			case CONFIRMAR -> confirmarReceita(dados.telefone(), CorrecaoReceita.vazia(), TEXTO_CONFIRMAR, null);
			case CORRECAO -> confirmarReceita(dados.telefone(), interpretacao.paraCorrecao(), TEXTO_VAZIO, null);
			case NAO_ENTENDIDO -> enviar(dados.telefone(), RegraMensagemReceita.correcaoNaoEntendida());
		}
	}

	private void confirmarDose(String telefone, String corpo, String textoBotao) {
		boolean confirmou = processarRespostaMensagemUseCase.executar(telefone, corpo, textoBotao);
		if (confirmou) {
			enviar(telefone, RegraMensagemReceita.primeiraDoseRegistrada());
		}
	}

	private void corrigirOuConfirmarReceita(DadosMensagemRecebida dados) {
		Optional<CorrecaoReceita> correcaoParseada = RegraParseCorrecaoReceita.parsear(dados.corpoOuBotao());
		boolean confirmacaoDeReceita = RegraRespostaPaciente.ehConfirmacaoDeReceita(dados.corpo(), dados.textoBotao());
		if (correcaoParseada.isEmpty() && !confirmacaoDeReceita) {
			enviar(dados.telefone(), RegraMensagemReceita.correcaoNaoEntendida());
			return;
		}
		confirmarReceita(
				dados.telefone(), correcaoParseada.orElse(CorrecaoReceita.vazia()), dados.corpo(), dados.textoBotao());
	}

	private void confirmarReceita(String telefone, CorrecaoReceita correcao, String corpo, String textoBotao) {
		try {
			String medicamento = receitaClientGateway.confirmarPorTelefone(telefone, correcao);
			enviar(telefone, RegraMensagemReceita.confirmada(medicamento));
		} catch (ReceitaPendenteNaoEncontradaException e) {
			processarRespostaMensagemUseCase.executar(telefone, corpo, textoBotao);
		} catch (DadosReceitaIncompletosException e) {
			enviar(telefone, RegraMensagemReceita.pedirCamposPendentes(e.getCamposPendentes()));
		}
	}

	private void enviar(String telefone, String texto) {
		mensageriaGateway.enviarMensagem(new ContatoWhatsApp(telefone), new ConteudoMensagem.Texto(texto));
	}
}
