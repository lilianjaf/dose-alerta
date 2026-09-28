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

/**
 * Orquestra o WhatsApp de ponta a ponta: identifica quem está falando (autocadastrando se preciso), extrai a
 * receita quando chega uma foto, e confirma quando chega uma resposta de texto. Quando não há receita pendente
 * para o telefone, cai no fluxo de rotina já existente ({@link ProcessarRespostaMensagemUseCase}), que continua
 * cuidando da confirmação do alarme (não é tocado por esta classe).
 */
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
		// Recém-criado: esta é a primeira mensagem desse telefone, ainda não perguntamos nada — não interpretar
		// o conteúdo dela (mesmo que seja texto) como resposta ao número de inscrição. Só a PRÓXIMA mensagem é a
		// resposta.
		if (!identificacao.recemCriado() && dados.temTexto()) {
			completarCadastro(dados);
		} else {
			enviar(dados.telefone(), RegraMensagemReceita.pedirNumeroInscricaoSus());
		}
	}

	private void completarCadastro(DadosMensagemRecebida dados) {
		// O nome nunca vem do paciente nem é usado em nenhuma mensagem: o número de inscrição só serve como
		// chave de busca no SUS (evita mostrar um nome errado por causa de um número digitado incorretamente).
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
		Optional<CorrecaoReceita> correcaoParseada = RegraParseCorrecaoReceita.parsear(dados.corpoOuBotao());
		// Nem "CONFIRMAR" nem o formato de correção reconhecido: não confirma a receita com dados errados só
		// porque não entendeu a resposta — antes disso o texto qualquer virava confirmação silenciosa.
		if (correcaoParseada.isEmpty() && !RegraRespostaPaciente.ehConfirmacao(dados.corpo(), dados.textoBotao())) {
			enviar(dados.telefone(), RegraMensagemReceita.correcaoNaoEntendida());
			return;
		}
		CorrecaoReceita correcao = correcaoParseada.orElse(CorrecaoReceita.vazia());
		try {
			String medicamento = receitaClientGateway.confirmarPorTelefone(dados.telefone(), correcao);
			enviar(dados.telefone(), RegraMensagemReceita.confirmada(medicamento));
		} catch (ReceitaPendenteNaoEncontradaException e) {
			// Sem receita pendente: pode ser a confirmação de rotina de um alarme já disparado.
			processarRespostaMensagemUseCase.executar(dados.telefone(), dados.corpo(), dados.textoBotao());
		} catch (DadosReceitaIncompletosException e) {
			enviar(dados.telefone(), RegraMensagemReceita.pedirCamposPendentes(e.getCamposPendentes()));
		}
	}

	private void enviar(String telefone, String texto) {
		mensageriaGateway.enviarMensagem(new ContatoWhatsApp(telefone), new ConteudoMensagem.Texto(texto));
	}
}
