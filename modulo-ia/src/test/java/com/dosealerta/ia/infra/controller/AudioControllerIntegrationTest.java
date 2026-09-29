package com.dosealerta.ia.infra.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dosealerta.ia.core.dto.AudioInterpretadoOutput;
import com.dosealerta.ia.core.dto.IntencaoAudio;
import com.dosealerta.ia.core.gateway.InterpretadorAudioGateway;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AudioControllerIntegrationTest {

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void propriedadesDinamicas(DynamicPropertyRegistry registry) throws Exception {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		registry.add(
				"security.jwt.public-key",
				() -> Base64.getEncoder().encodeToString(keyPairGenerator.generateKeyPair().getPublic().getEncoded()));
	}

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private InterpretadorAudioGateway interpretadorAudioGateway;

	@Test
	void devePermitirInterpretarAudioSemAutenticacao() throws Exception {
		when(interpretadorAudioGateway.interpretar(any(), anyString()))
				.thenReturn(new AudioInterpretadoOutput(IntencaoAudio.TOMEI, null, null, null));

		var audio = new MockMultipartFile("audio", "audio.ogg", "audio/ogg", new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/audio/interpretar").file(audio))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.intencao").value("TOMEI"));
	}

	@Test
	void deveRejeitarAudioVazio() throws Exception {
		var audio = new MockMultipartFile("audio", "audio.ogg", "audio/ogg", new byte[0]);

		mockMvc.perform(multipart("/audio/interpretar").file(audio)).andExpect(status().isBadRequest());
	}

	@Test
	void deveDevolverACorrecaoQuandoAIntencaoForCorrecao() throws Exception {
		when(interpretadorAudioGateway.interpretar(any(), anyString()))
				.thenReturn(new AudioInterpretadoOutput(IntencaoAudio.CORRECAO, "1 comprimido", 8, 7));

		var audio = new MockMultipartFile("audio", "audio.ogg", "audio/ogg", new byte[] {1, 2, 3});

		mockMvc.perform(multipart("/audio/interpretar").file(audio))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.intencao").value("CORRECAO"))
				.andExpect(jsonPath("$.dose").value("1 comprimido"))
				.andExpect(jsonPath("$.frequenciaHoras").value(8))
				.andExpect(jsonPath("$.duracaoDias").value(7));
	}
}
