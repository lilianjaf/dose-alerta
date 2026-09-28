package com.dosealerta.mensageria.core.gateway;

import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import com.dosealerta.mensageria.core.dto.ReceitaExtraidaResultado;
import java.time.Instant;
import java.util.UUID;

public interface ReceitaClientGateway {

	ReceitaExtraidaResultado extrair(
			UUID pacienteId, String telefone, Instant horarioInicial, byte[] imagem, String tipoConteudo);

	/**
	 * @throws com.dosealerta.mensageria.core.exception.ReceitaPendenteNaoEncontradaException se não há receita
	 *     aguardando confirmação para o telefone
	 * @throws com.dosealerta.mensageria.core.exception.DadosReceitaIncompletosException se, depois de aplicar a
	 *     correção, ainda faltar dose/frequência/duração
	 */
	String confirmarPorTelefone(String telefone, CorrecaoReceita correcao);
}
