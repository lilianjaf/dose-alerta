package com.dosealerta.mensageria.infra.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

	private static final String FUSO_HORARIO = "America/Sao_Paulo";

	@Bean
	public Clock clock() {
		return Clock.system(ZoneId.of(FUSO_HORARIO));
	}
}
