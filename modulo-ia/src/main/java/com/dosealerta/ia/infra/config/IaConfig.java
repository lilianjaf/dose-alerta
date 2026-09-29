package com.dosealerta.ia.infra.config;

import com.dosealerta.ia.core.gateway.CorrelacaoGateway;
import com.dosealerta.ia.core.gateway.ExtratorReceitaGateway;
import com.dosealerta.ia.core.gateway.FeedbackExtracaoRepositoryGateway;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;
import com.dosealerta.ia.core.gateway.LogGateway;
import com.dosealerta.ia.core.gateway.OutboxEventRepositoryGateway;
import com.dosealerta.ia.core.gateway.ReceitaRepositoryGateway;
import com.dosealerta.ia.core.gateway.SchedulerClientGateway;
import com.dosealerta.ia.core.gateway.TransactionGateway;
import com.dosealerta.ia.core.rules.buscar.BuscarReceitaDeveExistirRule;
import com.dosealerta.ia.core.rules.buscar.BuscarReceitaIdDeveSerInformadoRule;
import com.dosealerta.ia.core.rules.buscar.ValidadorBuscaReceitaRule;
import com.dosealerta.ia.core.rules.confirmar.ConfirmarDadosDaReceitaDevemEstarCompletosRule;
import com.dosealerta.ia.core.rules.confirmar.ConfirmarDoseInformadaNaoDeveEstarEmBrancoRule;
import com.dosealerta.ia.core.rules.confirmar.ConfirmarDuracaoDeveEstarNaFaixaRule;
import com.dosealerta.ia.core.rules.confirmar.ConfirmarFrequenciaDeveEstarNaFaixaRule;
import com.dosealerta.ia.core.rules.confirmar.ConfirmarMedicamentoInformadoNaoDeveEstarEmBrancoRule;
import com.dosealerta.ia.core.rules.confirmar.ConfirmarReceitaDeveEstarAguardandoConfirmacaoRule;
import com.dosealerta.ia.core.rules.confirmar.ConfirmarReceitaDeveExistirRule;
import com.dosealerta.ia.core.rules.confirmar.ValidadorConfirmacaoReceitaRule;
import com.dosealerta.ia.core.rules.confirmarportelefone.ConfirmarPorTelefoneReceitaPendenteDeveExistirRule;
import com.dosealerta.ia.core.rules.confirmarportelefone.ConfirmarPorTelefoneTelefoneDevePreenchidoRule;
import com.dosealerta.ia.core.rules.confirmarportelefone.ConfirmarPorTelefoneTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.ia.core.rules.confirmarportelefone.ValidadorConfirmacaoPorTelefoneRule;
import com.dosealerta.ia.core.rules.extrair.ExtrairHorarioInicialDeveSerInformadoRule;
import com.dosealerta.ia.core.rules.extrair.ExtrairImagemDevePreenchidaRule;
import com.dosealerta.ia.core.rules.extrair.ExtrairImagemNaoDeveExcederTamanhoMaximoRule;
import com.dosealerta.ia.core.rules.extrair.ExtrairPacienteIdDeveSerInformadoRule;
import com.dosealerta.ia.core.rules.extrair.ExtrairTelefoneDevePreenchidoRule;
import com.dosealerta.ia.core.rules.extrair.ExtrairTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.ia.core.rules.extrair.ValidadorExtracaoReceitaRule;
import com.dosealerta.ia.core.rules.receitaextraida.ReceitaExtraidaDeveSerReceitaMedicaRule;
import com.dosealerta.ia.core.rules.receitaextraida.ReceitaExtraidaDeveTerMedicamentosRule;
import com.dosealerta.ia.core.rules.receitaextraida.ReceitaExtraidaDeveTerNomeDoPrescritorRule;
import com.dosealerta.ia.core.rules.receitaextraida.ReceitaExtraidaDeveTerRegistroProfissionalPlausivelRule;
import com.dosealerta.ia.core.rules.receitaextraida.ValidadorReceitaExtraidaRule;
import com.dosealerta.ia.core.usecase.BuscarReceitaUseCase;
import com.dosealerta.ia.core.usecase.BuscarReceitaUseCaseImpl;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaPorTelefoneUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaPorTelefoneUseCaseImpl;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaUseCase;
import com.dosealerta.ia.core.usecase.ConfirmarReceitaUseCaseImpl;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCase;
import com.dosealerta.ia.core.usecase.ExtrairReceitaUseCaseImpl;
import com.dosealerta.ia.core.usecase.InterpretarAudioUseCase;
import com.dosealerta.ia.core.usecase.InterpretarAudioUseCaseImpl;
import com.dosealerta.ia.core.usecase.PublicarEventosPendentesUseCase;
import com.dosealerta.ia.core.usecase.PublicarEventosPendentesUseCaseImpl;
import com.dosealerta.ia.infra.client.ExtratorReceitaGatewayComMockDeEmergencia;
import com.dosealerta.ia.infra.client.MockExtratorReceitaGateway;
import com.dosealerta.ia.infra.decorator.LoggingBuscarReceitaUseCase;
import com.dosealerta.ia.infra.decorator.LoggingConfirmarReceitaPorTelefoneUseCase;
import com.dosealerta.ia.infra.decorator.LoggingConfirmarReceitaUseCase;
import com.dosealerta.ia.infra.decorator.LoggingExtrairReceitaUseCase;
import com.dosealerta.ia.infra.decorator.LoggingInterpretarAudioUseCase;
import com.dosealerta.ia.infra.decorator.LoggingPublicarEventosPendentesUseCase;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IaConfig {

	@Bean
	public ExtrairReceitaUseCase extrairReceitaUseCase(
			ExtratorReceitaGateway extratorReceitaGateway,
			ReceitaRepositoryGateway receitaRepositoryGateway,
			TransactionGateway transactionGateway,
			Clock clock,
			@Value("${ia.mock-de-emergencia-habilitado:false}") boolean mockDeEmergenciaHabilitado) {
		ExtratorReceitaGateway gateway = mockDeEmergenciaHabilitado
				? new ExtratorReceitaGatewayComMockDeEmergencia(extratorReceitaGateway)
				: extratorReceitaGateway;
		return criarExtrairReceita(gateway, receitaRepositoryGateway, transactionGateway, clock);
	}

	@Bean
	public ExtrairReceitaUseCase extrairReceitaUseCaseMock(
			ReceitaRepositoryGateway receitaRepositoryGateway, TransactionGateway transactionGateway, Clock clock) {
		return criarExtrairReceita(
				new MockExtratorReceitaGateway(), receitaRepositoryGateway, transactionGateway, clock);
	}

	@Bean
	public ConfirmarReceitaUseCase confirmarReceitaUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway,
			FeedbackExtracaoRepositoryGateway feedbackExtracaoRepositoryGateway,
			TransactionGateway transactionGateway,
			CorrelacaoGateway correlacaoGateway,
			Clock clock) {
		List<ValidadorConfirmacaoReceitaRule> rules = List.of(
				new ConfirmarMedicamentoInformadoNaoDeveEstarEmBrancoRule(),
				new ConfirmarDoseInformadaNaoDeveEstarEmBrancoRule(),
				new ConfirmarFrequenciaDeveEstarNaFaixaRule(),
				new ConfirmarDuracaoDeveEstarNaFaixaRule(),
				new ConfirmarReceitaDeveExistirRule(),
				new ConfirmarReceitaDeveEstarAguardandoConfirmacaoRule(),
				new ConfirmarDadosDaReceitaDevemEstarCompletosRule());
		return new LoggingConfirmarReceitaUseCase(new ConfirmarReceitaUseCaseImpl(
				receitaRepositoryGateway,
				feedbackExtracaoRepositoryGateway,
				transactionGateway,
				correlacaoGateway,
				clock,
				rules));
	}

	@Bean
	public ConfirmarReceitaPorTelefoneUseCase confirmarReceitaPorTelefoneUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway, ConfirmarReceitaUseCase confirmarReceitaUseCase) {
		List<ValidadorConfirmacaoPorTelefoneRule> rules = List.of(
				new ConfirmarPorTelefoneTelefoneDevePreenchidoRule(),
				new ConfirmarPorTelefoneReceitaPendenteDeveExistirRule(),
				new ConfirmarPorTelefoneTelefoneDeveTerFormatoValidoRule());
		return new LoggingConfirmarReceitaPorTelefoneUseCase(
				new ConfirmarReceitaPorTelefoneUseCaseImpl(receitaRepositoryGateway, confirmarReceitaUseCase, rules));
	}

	@Bean
	public BuscarReceitaUseCase buscarReceitaUseCase(ReceitaRepositoryGateway receitaRepositoryGateway) {
		List<ValidadorBuscaReceitaRule> rules =
				List.of(new BuscarReceitaIdDeveSerInformadoRule(), new BuscarReceitaDeveExistirRule());
		return new LoggingBuscarReceitaUseCase(new BuscarReceitaUseCaseImpl(receitaRepositoryGateway, rules));
	}

	@Bean
	public InterpretarAudioUseCase interpretarAudioUseCase(InterpretadorAudioGateway interpretadorAudioGateway) {
		return new LoggingInterpretarAudioUseCase(new InterpretarAudioUseCaseImpl(interpretadorAudioGateway));
	}

	@Bean
	public PublicarEventosPendentesUseCase publicarEventosPendentesUseCase(
			ReceitaRepositoryGateway receitaRepositoryGateway,
			OutboxEventRepositoryGateway outboxEventRepositoryGateway,
			SchedulerClientGateway schedulerClientGateway,
			LogGateway logGateway,
			CorrelacaoGateway correlacaoGateway,
			Clock clock) {
		return new LoggingPublicarEventosPendentesUseCase(new PublicarEventosPendentesUseCaseImpl(
				receitaRepositoryGateway,
				outboxEventRepositoryGateway,
				schedulerClientGateway,
				logGateway,
				correlacaoGateway,
				clock));
	}

	private ExtrairReceitaUseCase criarExtrairReceita(
			ExtratorReceitaGateway extratorReceitaGateway,
			ReceitaRepositoryGateway receitaRepositoryGateway,
			TransactionGateway transactionGateway,
			Clock clock) {
		List<ValidadorExtracaoReceitaRule> regrasDeEntrada = List.of(
				new ExtrairImagemDevePreenchidaRule(),
				new ExtrairImagemNaoDeveExcederTamanhoMaximoRule(),
				new ExtrairPacienteIdDeveSerInformadoRule(),
				new ExtrairTelefoneDevePreenchidoRule(),
				new ExtrairHorarioInicialDeveSerInformadoRule(),
				new ExtrairTelefoneDeveTerFormatoValidoRule());
		List<ValidadorReceitaExtraidaRule> regrasDaReceitaExtraida = List.of(
				new ReceitaExtraidaDeveSerReceitaMedicaRule(),
				new ReceitaExtraidaDeveTerNomeDoPrescritorRule(),
				new ReceitaExtraidaDeveTerRegistroProfissionalPlausivelRule(),
				new ReceitaExtraidaDeveTerMedicamentosRule());
		return new LoggingExtrairReceitaUseCase(new ExtrairReceitaUseCaseImpl(
				extratorReceitaGateway,
				receitaRepositoryGateway,
				transactionGateway,
				clock,
				regrasDeEntrada,
				regrasDaReceitaExtraida));
	}
}
