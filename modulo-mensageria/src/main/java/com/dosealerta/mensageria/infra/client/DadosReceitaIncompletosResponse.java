package com.dosealerta.mensageria.infra.client;

import java.util.List;

record DadosReceitaIncompletosResponse(String mensagem, List<String> camposPendentes) {
}
