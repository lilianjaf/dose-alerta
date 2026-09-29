package com.dosealerta.ia;

import java.time.Clock;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class RelogioFixoTestConfig {

	@Bean
	@Primary
	public Clock relogioFixo() {
		return IaFixtures.CLOCK_FIXO;
	}
}
