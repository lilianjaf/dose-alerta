package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.domain.ConteudoMensagem;
import com.dosealerta.mensageria.core.domain.ContatoWhatsApp;
import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;
import com.dosealerta.mensageria.core.dto.IdentificarPacienteResultado;
import com.dosealerta.mensageria.core.dto.ReceitaExtraidaResultado;
import com.dosealerta.mensageria.core.exception.DadosReceitaIncompletosException;
import com.dosealerta.mensageria.core.exception.NumeroInscricaoSusNaoEncontradoException;
import com.dosealerta.mensageria.core.exception.ReceitaPendenteNaoEncontradaException;
import com.dosealerta.mensageria.core.gateway.MediaDownloadGateway;
import com.dosealerta.mensageria.core.gateway.MensageriaGateway;
import com.dosealerta.mensageria.core.gateway.PacienteClientGateway;
import com.dosealerta.mensageria.core.gateway.ReceitaClientGateway;
import com.dosealerta.mensageria.core.rules.RegraMensagemReceita;
import com.dosealerta.mensageria.core.rules.RegraParseCorrecaoReceita;
import com.dosealerta.mensageria.core.rules.RegraRespostaPaciente;
import java.net.URI;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessarMensagemRecebidaUseCase {

	private static final Logger log = LoggerFactory.getLogger(ProcessarMensagemRecebidaUseCase.class);

	private final PacienteClientGateway pacienteClientGateway;
	private final ReceitaClientGateway receitaClientGateway;
	private final MediaDownloadGateway mediaDownloadGateway;
	private final MensageriaGateway mensageriaGateway;
	private final ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase;

	public ProcessarMensagemRecebidaUseCase(
			PacienteClientGateway pacienteClientGateway,
			ReceitaClientGateway receitaClientGateway,
			MediaDownloadGateway mediaDownloadGateway,
			MensageriaGateway mensageriaGateway,
			ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase) {
		this.pacienteClientGateway = pacienteClientGateway;
		this.receitaClientGateway = receitaClientGateway;
		this.mediaDownloadGateway = mediaDownloadGateway;
		this.mensageriaGateway = mensageriaGateway;
		this.processarRespostaMensagemUseCase = processarRespostaMensagemUseCase;
	}

	public void executar(DadosMensagemRecebida dados) {
		try {
			processar(dados);
		} catch (RuntimeException e) {
			log.warn("Falha ao processar mensagem recebida de {}", dados.telefone(), e);
		}
	}

	private void processar(DadosMensagemRecebida dados) {
		IdentificarPacienteResultado identificacao = pacienteClientGateway.identificar(dados.telefone());

		if (!identificacao.cadastroCompleto()) {
			identificarOuCompletarCadastro(dados, identificacao);
			return;
		}
		if (dados.temFoto()) {
			extrairReceita(dados, identificacao);
			return;
		}
		if (!dados.temTexto()) {
			enviar(dados.telefone(), RegraMensagemReceita.ajuda());
			return;
		}
		confirmarOuCairNaRotina(dados);
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
				identificacao.pacienteId(), dados.telefone(), Instant.now(), imagem, dados.mediaContentType0());

		enviar(dados.telefone(), RegraMensagemReceita.resumoExtracao(resultado.receitas(), resultado.naoProcessados()));
	}

	private void confirmarOuCairNaRotina(DadosMensagemRecebida dados) {
		if (RegraRespostaPaciente.ehNegacao(dados.corpo(), dados.textoBotao())) {
			enviar(dados.telefone(), RegraMensagemReceita.primeiraDoseAindaNaoTomada());
			return;
		}
		// "TOMEI" é resposta à pergunta sobre a dose do alarme, nunca confirmação de receita — checa isso antes
		// de tentar confirmarPorTelefone, senão "TOMEI" confirmaria de vez uma outra receita ainda pendente na
		// fila (ex: quando a foto trouxe mais de um medicamento e só um foi confirmado até aqui).
		if (RegraRespostaPaciente.ehConfirmacaoDeDose(dados.corpo(), dados.textoBotao())) {
			boolean confirmou = processarRespostaMensagemUseCase.executar(dados.telefone(), dados.corpo(), dados.textoBotao());
			if (confirmou) {
				enviar(dados.telefone(), RegraMensagemReceita.primeiraDoseRegistrada());
			}
			return;
		}

		Optional<CorrecaoReceita> correcaoParseada = RegraParseCorrecaoReceita.parsear(dados.corpoOuBotao());
		boolean confirmacaoDeReceita = RegraRespostaPaciente.ehConfirmacaoDeReceita(dados.corpo(), dados.textoBotao());
		if (correcaoParseada.isEmpty() && !confirmacaoDeReceita) {
			enviar(dados.telefone(), RegraMensagemReceita.correcaoNaoEntendida());
			return;
		}
		CorrecaoReceita correcao = correcaoParseada.orElse(CorrecaoReceita.vazia());
		try {
			String medicamento = receitaClientGateway.confirmarPorTelefone(dados.telefone(), correcao);
			enviar(dados.telefone(), RegraMensagemReceita.confirmada(medicamento));
		} catch (ReceitaPendenteNaoEncontradaException e) {
			// Sem receita pendente: confirmação de rotina de um alarme já disparado.
			processarRespostaMensagemUseCase.executar(dados.telefone(), dados.corpo(), dados.textoBotao());
		} catch (DadosReceitaIncompletosException e) {
			enviar(dados.telefone(), RegraMensagemReceita.pedirCamposPendentes(e.getCamposPendentes()));
		}
	}

	private void enviar(String telefone, String texto) {
		mensageriaGateway.enviarMensagem(new ContatoWhatsApp(telefone), new ConteudoMensagem.Texto(texto));
	}
}
