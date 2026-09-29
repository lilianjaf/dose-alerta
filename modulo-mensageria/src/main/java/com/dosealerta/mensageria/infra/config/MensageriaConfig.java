package com.dosealerta.mensageria.infra.config;

import com.dosealerta.mensageria.core.gateway.AlarmeClientGateway;
import com.dosealerta.mensageria.core.gateway.LogGateway;
import com.dosealerta.mensageria.core.gateway.MediaDownloadGateway;
import com.dosealerta.mensageria.core.gateway.MensageriaGateway;
import com.dosealerta.mensageria.core.gateway.PacienteClientGateway;
import com.dosealerta.mensageria.core.gateway.ReceitaClientGateway;
import com.dosealerta.mensageria.core.rules.confirmacaoligacao.ConfirmacaoLigacaoTelefoneDevePreenchidoRule;
import com.dosealerta.mensageria.core.rules.confirmacaoligacao.ValidadorConfirmacaoLigacaoRule;
import com.dosealerta.mensageria.core.rules.mensagemrecebida.MensagemRecebidaTelefoneDevePreenchidoRule;
import com.dosealerta.mensageria.core.rules.mensagemrecebida.ValidadorMensagemRecebidaRule;
import com.dosealerta.mensageria.core.rules.respostamensagem.RespostaMensagemTelefoneDevePreenchidoRule;
import com.dosealerta.mensageria.core.rules.respostamensagem.ValidadorRespostaMensagemRule;
import com.dosealerta.mensageria.core.rules.statusligacao.StatusLigacaoTelefoneDevePreenchidoRule;
import com.dosealerta.mensageria.core.rules.statusligacao.ValidadorStatusLigacaoRule;
import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarConfirmacaoLigacaoUseCaseImpl;
import com.dosealerta.mensageria.core.usecase.ProcessarMensagemRecebidaUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarMensagemRecebidaUseCaseImpl;
import com.dosealerta.mensageria.core.usecase.ProcessarRespostaMensagemUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarRespostaMensagemUseCaseImpl;
import com.dosealerta.mensageria.core.usecase.ProcessarStatusLigacaoUseCase;
import com.dosealerta.mensageria.core.usecase.ProcessarStatusLigacaoUseCaseImpl;
import com.dosealerta.mensageria.infra.decorator.LoggingProcessarConfirmacaoLigacaoUseCase;
import com.dosealerta.mensageria.infra.decorator.LoggingProcessarMensagemRecebidaUseCase;
import com.dosealerta.mensageria.infra.decorator.LoggingProcessarRespostaMensagemUseCase;
import com.dosealerta.mensageria.infra.decorator.LoggingProcessarStatusLigacaoUseCase;
import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MensageriaConfig {

	@Bean
	public ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase(
			AlarmeClientGateway alarmeClientGateway, LogGateway logGateway) {
		List<ValidadorRespostaMensagemRule> rules = List.of(new RespostaMensagemTelefoneDevePreenchidoRule());
		return new LoggingProcessarRespostaMensagemUseCase(
				new ProcessarRespostaMensagemUseCaseImpl(alarmeClientGateway, logGateway, rules));
	}

	@Bean
	public ProcessarMensagemRecebidaUseCase processarMensagemRecebidaUseCase(
			PacienteClientGateway pacienteClientGateway,
			ReceitaClientGateway receitaClientGateway,
			MediaDownloadGateway mediaDownloadGateway,
			MensageriaGateway mensageriaGateway,
			ProcessarRespostaMensagemUseCase processarRespostaMensagemUseCase,
			LogGateway logGateway,
			Clock clock) {
		List<ValidadorMensagemRecebidaRule> rules = List.of(new MensagemRecebidaTelefoneDevePreenchidoRule());
		return new LoggingProcessarMensagemRecebidaUseCase(new ProcessarMensagemRecebidaUseCaseImpl(
				pacienteClientGateway,
				receitaClientGateway,
				mediaDownloadGateway,
				mensageriaGateway,
				processarRespostaMensagemUseCase,
				logGateway,
				clock,
				rules));
	}

	@Bean
	public ProcessarConfirmacaoLigacaoUseCase processarConfirmacaoLigacaoUseCase(
			AlarmeClientGateway alarmeClientGateway, LogGateway logGateway) {
		List<ValidadorConfirmacaoLigacaoRule> rules = List.of(new ConfirmacaoLigacaoTelefoneDevePreenchidoRule());
		return new LoggingProcessarConfirmacaoLigacaoUseCase(
				new ProcessarConfirmacaoLigacaoUseCaseImpl(alarmeClientGateway, logGateway, rules));
	}

	@Bean
	public ProcessarStatusLigacaoUseCase processarStatusLigacaoUseCase(
			AlarmeClientGateway alarmeClientGateway, LogGateway logGateway) {
		List<ValidadorStatusLigacaoRule> rules = List.of(new StatusLigacaoTelefoneDevePreenchidoRule());
		return new LoggingProcessarStatusLigacaoUseCase(
				new ProcessarStatusLigacaoUseCaseImpl(alarmeClientGateway, logGateway, rules));
	}
}
