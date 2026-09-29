package com.dosealerta.usuario.infra.config;

import com.dosealerta.usuario.core.gateway.AutenticacaoGateway;
import com.dosealerta.usuario.core.gateway.CadastroSusGateway;
import com.dosealerta.usuario.core.gateway.PacienteRepositoryGateway;
import com.dosealerta.usuario.core.gateway.SenhaGateway;
import com.dosealerta.usuario.core.rules.autenticacao.AutenticacaoPacienteDeveExistirRule;
import com.dosealerta.usuario.core.rules.autenticacao.AutenticacaoSenhaDeveConferirRule;
import com.dosealerta.usuario.core.rules.autenticacao.AutenticacaoSenhaDevePreenchidaRule;
import com.dosealerta.usuario.core.rules.autenticacao.AutenticacaoTelefoneDevePreenchidoRule;
import com.dosealerta.usuario.core.rules.autenticacao.ValidadorAutenticacaoRule;
import com.dosealerta.usuario.core.rules.cadastro.CadastroNomeDevePreenchidoRule;
import com.dosealerta.usuario.core.rules.cadastro.CadastroSenhaDevePreenchidaRule;
import com.dosealerta.usuario.core.rules.cadastro.CadastroSenhaDeveTerTamanhoMinimoRule;
import com.dosealerta.usuario.core.rules.cadastro.CadastroTelefoneDevePreenchidoRule;
import com.dosealerta.usuario.core.rules.cadastro.CadastroTelefoneDeveSerUnicoRule;
import com.dosealerta.usuario.core.rules.cadastro.CadastroTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.usuario.core.rules.cadastro.ValidadorCadastroPacienteRule;
import com.dosealerta.usuario.core.rules.completarcadastro.CompletarCadastroNumeroInscricaoSusDeveExistirRule;
import com.dosealerta.usuario.core.rules.completarcadastro.CompletarCadastroNumeroInscricaoSusDevePreenchidoRule;
import com.dosealerta.usuario.core.rules.completarcadastro.CompletarCadastroNumeroInscricaoSusDeveTerQuinzeDigitosRule;
import com.dosealerta.usuario.core.rules.completarcadastro.CompletarCadastroPacienteDeveExistirRule;
import com.dosealerta.usuario.core.rules.completarcadastro.CompletarCadastroTelefoneDevePreenchidoRule;
import com.dosealerta.usuario.core.rules.completarcadastro.CompletarCadastroTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.usuario.core.rules.completarcadastro.ValidadorCompletarCadastroRule;
import com.dosealerta.usuario.core.rules.identificacao.IdentificacaoTelefoneDevePreenchidoRule;
import com.dosealerta.usuario.core.rules.identificacao.IdentificacaoTelefoneDeveTerFormatoValidoRule;
import com.dosealerta.usuario.core.rules.identificacao.ValidadorIdentificacaoPacienteRule;
import com.dosealerta.usuario.core.usecase.AutenticarPacienteUseCase;
import com.dosealerta.usuario.core.usecase.AutenticarPacienteUseCaseImpl;
import com.dosealerta.usuario.core.usecase.CadastrarPacienteUseCase;
import com.dosealerta.usuario.core.usecase.CadastrarPacienteUseCaseImpl;
import com.dosealerta.usuario.core.usecase.CompletarCadastroUseCase;
import com.dosealerta.usuario.core.usecase.CompletarCadastroUseCaseImpl;
import com.dosealerta.usuario.core.usecase.IdentificarPacientePorTelefoneUseCase;
import com.dosealerta.usuario.core.usecase.IdentificarPacientePorTelefoneUseCaseImpl;
import com.dosealerta.usuario.infra.decorator.LoggingAutenticarPacienteUseCase;
import com.dosealerta.usuario.infra.decorator.LoggingCadastrarPacienteUseCase;
import com.dosealerta.usuario.infra.decorator.LoggingCompletarCadastroUseCase;
import com.dosealerta.usuario.infra.decorator.LoggingIdentificarPacientePorTelefoneUseCase;
import java.time.Clock;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioConfig {

	@Bean
	public CadastrarPacienteUseCase cadastrarPacienteUseCase(
			PacienteRepositoryGateway pacienteRepositoryGateway, SenhaGateway senhaGateway, Clock clock) {
		List<ValidadorCadastroPacienteRule> rules = List.of(
				new CadastroNomeDevePreenchidoRule(),
				new CadastroTelefoneDevePreenchidoRule(),
				new CadastroSenhaDevePreenchidaRule(),
				new CadastroTelefoneDeveTerFormatoValidoRule(),
				new CadastroSenhaDeveTerTamanhoMinimoRule(),
				new CadastroTelefoneDeveSerUnicoRule());
		return new LoggingCadastrarPacienteUseCase(
				new CadastrarPacienteUseCaseImpl(pacienteRepositoryGateway, senhaGateway, clock, rules));
	}

	@Bean
	public AutenticarPacienteUseCase autenticarPacienteUseCase(
			PacienteRepositoryGateway pacienteRepositoryGateway,
			SenhaGateway senhaGateway,
			AutenticacaoGateway autenticacaoGateway) {
		List<ValidadorAutenticacaoRule> rules = List.of(
				new AutenticacaoTelefoneDevePreenchidoRule(),
				new AutenticacaoSenhaDevePreenchidaRule(),
				new AutenticacaoPacienteDeveExistirRule(),
				new AutenticacaoSenhaDeveConferirRule());
		return new LoggingAutenticarPacienteUseCase(new AutenticarPacienteUseCaseImpl(
				pacienteRepositoryGateway, senhaGateway, autenticacaoGateway, rules));
	}

	@Bean
	public IdentificarPacientePorTelefoneUseCase identificarPacientePorTelefoneUseCase(
			PacienteRepositoryGateway pacienteRepositoryGateway, CadastroSusGateway cadastroSusGateway, Clock clock) {
		List<ValidadorIdentificacaoPacienteRule> rules = List.of(new IdentificacaoTelefoneDevePreenchidoRule(),
				new IdentificacaoTelefoneDeveTerFormatoValidoRule());
		return new LoggingIdentificarPacientePorTelefoneUseCase(new IdentificarPacientePorTelefoneUseCaseImpl(
				pacienteRepositoryGateway, cadastroSusGateway, clock, rules));
	}

	@Bean
	public CompletarCadastroUseCase completarCadastroUseCase(
			PacienteRepositoryGateway pacienteRepositoryGateway, CadastroSusGateway cadastroSusGateway) {
		List<ValidadorCompletarCadastroRule> rules = List.of(
				new CompletarCadastroTelefoneDevePreenchidoRule(),
				new CompletarCadastroNumeroInscricaoSusDevePreenchidoRule(),
				new CompletarCadastroTelefoneDeveTerFormatoValidoRule(),
				new CompletarCadastroNumeroInscricaoSusDeveTerQuinzeDigitosRule(),
				new CompletarCadastroPacienteDeveExistirRule(),
				new CompletarCadastroNumeroInscricaoSusDeveExistirRule());
		return new LoggingCompletarCadastroUseCase(
				new CompletarCadastroUseCaseImpl(pacienteRepositoryGateway, cadastroSusGateway, rules));
	}
}
