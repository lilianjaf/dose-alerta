package com.dosealerta.ia.core.gateway;

import com.dosealerta.ia.core.dto.ReceitaExtraida;

public interface ExtratorReceitaGateway {

	ReceitaExtraida extrair(byte[] imagem);
}
