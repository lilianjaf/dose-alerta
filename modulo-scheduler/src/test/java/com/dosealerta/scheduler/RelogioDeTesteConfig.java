package com.dosealerta.scheduler;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class RelogioDeTesteConfig {

	@Bean
	@Primary
	public RelogioDeTeste relogioDeTeste() {
		return new RelogioDeTeste(SchedulerFixtures.INSTANTE_FIXO, SchedulerFixtures.FUSO_HORARIO);
	}
}
