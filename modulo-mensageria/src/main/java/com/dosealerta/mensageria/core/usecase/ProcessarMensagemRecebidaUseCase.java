package com.dosealerta.mensageria.core.usecase;

import com.dosealerta.mensageria.core.dto.DadosMensagemRecebida;

public interface ProcessarMensagemRecebidaUseCase {

	void executar(DadosMensagemRecebida dados);
}
