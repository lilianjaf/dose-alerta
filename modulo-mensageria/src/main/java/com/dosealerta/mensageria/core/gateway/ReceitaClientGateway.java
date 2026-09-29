package com.dosealerta.mensageria.core.gateway;

import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import com.dosealerta.mensageria.core.dto.ReceitaExtraidaResultado;
import java.time.Instant;
import java.util.UUID;

public interface ReceitaClientGateway {

	ReceitaExtraidaResultado extrair(
			UUID pacienteId, String telefone, Instant horarioInicial, byte[] imagem, String tipoConteudo);

	String confirmarPorTelefone(String telefone, CorrecaoReceita correcao);
}
