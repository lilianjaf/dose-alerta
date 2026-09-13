package com.dosealerta.mensageria.infra.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

	private final String accountSid;
	private final String authToken;

	public TwilioConfig(
			@Value("${twilio.account-sid}") String accountSid, @Value("${twilio.auth-token}") String authToken) {
		this.accountSid = accountSid;
		this.authToken = authToken;
	}

	@PostConstruct
	void inicializarClienteTwilio() {
		Twilio.init(accountSid, authToken);
	}
}
