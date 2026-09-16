package com.dosealerta.scheduler.core.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.dosealerta.scheduler.core.domain.Alarme;
import com.dosealerta.scheduler.core.domain.EtapaEscalonamento;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegraEscalonamentoAlarmeTest {

	private static final Instant HORARIO_ALVO = Instant.parse("2026-01-01T12:00:00Z");

	private Alarme alarmePendente() {
		return Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", HORARIO_ALVO);
	}

	@Test
	void naoDeveEnviarAntesDoHorarioAlvo() {
		Alarme alarme = alarmePendente();

		DecisaoEscalonamento decisao = RegraEscalonamentoAlarme.decidir(alarme, HORARIO_ALVO.minusSeconds(1));

		assertInstanceOf(DecisaoEscalonamento.Nada.class, decisao);
	}

	@Test
	void deveEnviarLembreteInicialNoHorarioAlvo() {
		Alarme alarme = alarmePendente();

		DecisaoEscalonamento decisao = RegraEscalonamentoAlarme.decidir(alarme, HORARIO_ALVO);

		var enviar = assertInstanceOf(DecisaoEscalonamento.Enviar.class, decisao);
		assertEquals(EtapaEscalonamento.LEMBRETE_INICIAL, enviar.etapa());
	}

	@Test
	void naoDeveReforcarAntesDosQuinzeMinutos() {
		Alarme alarme = alarmePendente();
		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, HORARIO_ALVO);

		DecisaoEscalonamento decisao = RegraEscalonamentoAlarme.decidir(alarme, HORARIO_ALVO.plusSeconds(60));

		assertInstanceOf(DecisaoEscalonamento.Nada.class, decisao);
	}

	@Test
	void deveReforcarAposQuinzeMinutosSemConfirmacao() {
		Alarme alarme = alarmePendente();
		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, HORARIO_ALVO);

		DecisaoEscalonamento decisao =
				RegraEscalonamentoAlarme.decidir(alarme, HORARIO_ALVO.plus(Duration.ofMinutes(15)));

		var enviar = assertInstanceOf(DecisaoEscalonamento.Enviar.class, decisao);
		assertEquals(EtapaEscalonamento.REFORCO, enviar.etapa());
	}

	@Test
	void deveLigarAposTrintaMinutosSemConfirmacao() {
		Alarme alarme = alarmePendente();
		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, HORARIO_ALVO);
		alarme.registrarEnvio(EtapaEscalonamento.REFORCO, HORARIO_ALVO.plus(Duration.ofMinutes(15)));

		DecisaoEscalonamento decisao =
				RegraEscalonamentoAlarme.decidir(alarme, HORARIO_ALVO.plus(Duration.ofMinutes(30)));

		var enviar = assertInstanceOf(DecisaoEscalonamento.Enviar.class, decisao);
		assertEquals(EtapaEscalonamento.LIGACAO, enviar.etapa());
	}

	@Test
	void deveFinalizarSemConfirmacaoAposQuarentaECincoMinutosDaLigacao() {
		Alarme alarme = alarmePendente();
		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, HORARIO_ALVO);
		alarme.registrarEnvio(EtapaEscalonamento.REFORCO, HORARIO_ALVO.plus(Duration.ofMinutes(15)));
		alarme.registrarEnvio(EtapaEscalonamento.LIGACAO, HORARIO_ALVO.plus(Duration.ofMinutes(30)));

		DecisaoEscalonamento decisao =
				RegraEscalonamentoAlarme.decidir(alarme, HORARIO_ALVO.plus(Duration.ofMinutes(45)));

		assertInstanceOf(DecisaoEscalonamento.FinalizarSemConfirmacao.class, decisao);
	}

	@Test
	void naoDeveQueimarEtapasQuandoOEscalonamentoFicaAtrasado() {
		// horarioAlvo bem no passado, simulando o scheduler retomando após um período parado
		Alarme alarme = Alarme.criar(UUID.randomUUID(), "+5511999999999", "Losartana", "50mg", HORARIO_ALVO.minus(Duration.ofHours(2)));
		Instant agora = HORARIO_ALVO;

		DecisaoEscalonamento primeiraDecisao = RegraEscalonamentoAlarme.decidir(alarme, agora);
		var enviar = assertInstanceOf(DecisaoEscalonamento.Enviar.class, primeiraDecisao);
		assertEquals(EtapaEscalonamento.LEMBRETE_INICIAL, enviar.etapa());
		alarme.registrarEnvio(enviar.etapa(), agora);

		// mesmo com o horário alvo há 2h no passado, a próxima etapa só deve sair 15min após o envio anterior
		DecisaoEscalonamento decisaoLogoEmSeguida = RegraEscalonamentoAlarme.decidir(alarme, agora.plusSeconds(1));
		assertInstanceOf(DecisaoEscalonamento.Nada.class, decisaoLogoEmSeguida);
	}

	@Test
	void naoDeveFazerNadaQuandoAlarmeJaConfirmado() {
		Alarme alarme = alarmePendente();
		alarme.registrarEnvio(EtapaEscalonamento.LEMBRETE_INICIAL, HORARIO_ALVO);
		alarme.confirmar(HORARIO_ALVO.plusSeconds(30));

		DecisaoEscalonamento decisao =
				RegraEscalonamentoAlarme.decidir(alarme, HORARIO_ALVO.plus(Duration.ofMinutes(30)));

		assertInstanceOf(DecisaoEscalonamento.Nada.class, decisao);
	}
}
