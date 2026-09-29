package com.dosealerta.mensageria;

import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;
import com.dosealerta.mensageria.core.dto.IdentificarPacienteResultado;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

public final class MensageriaFixtures {

	public static final Instant INSTANTE_FIXO = Instant.parse("2026-01-15T12:00:00Z");
	public static final Clock CLOCK_FIXO = Clock.fixed(INSTANTE_FIXO, ZoneId.of("America/Sao_Paulo"));
	public static final UUID PACIENTE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final String TELEFONE = "+5511999999999";
	public static final String VALOR_EM_BRANCO = "  ";
	public static final String URL_MIDIA = "https://twilio/media/1";
	public static final String TIPO_IMAGEM = "image/jpeg";
	public static final byte[] IMAGEM = {1, 2, 3};
	public static final String NUMERO_INSCRICAO_SUS = "700000000000001";

	private MensageriaFixtures() {
	}

	public static IdentificarPacienteResultado umPacienteCompleto() {
		return new IdentificarPacienteResultado(PACIENTE_ID, "Maria da Silva", true, false);
	}

	public static IdentificarPacienteResultado umPacienteIncompletoRecemCriado() {
		return new IdentificarPacienteResultado(PACIENTE_ID, null, false, true);
	}

	public static IdentificarPacienteResultado umPacienteIncompletoAntigo() {
		return new IdentificarPacienteResultado(PACIENTE_ID, null, false, false);
	}

	public static DadosMensagemRecebida umaMensagemDeTexto(String corpo) {
		return new DadosMensagemRecebida(TELEFONE, corpo, null, null, 0, null);
	}

	public static DadosMensagemRecebida umaMensagemSemConteudo() {
		return new DadosMensagemRecebida(TELEFONE, null, null, null, 0, null);
	}

	public static DadosMensagemRecebida umaMensagemComFoto() {
		return new DadosMensagemRecebida(TELEFONE, null, null, URL_MIDIA, 1, TIPO_IMAGEM);
	}

	public static DadosMensagemRecebida umaMensagemDoTelefone(String telefone) {
		return new DadosMensagemRecebida(telefone, "oi", null, null, 0, null);
	}

	public static CorrecaoReceita umaCorrecaoDeDose() {
		return new CorrecaoReceita(null, "1 comprimido", 8, 7);
	}
}
