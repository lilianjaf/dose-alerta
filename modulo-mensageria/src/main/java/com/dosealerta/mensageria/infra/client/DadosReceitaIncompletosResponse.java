package com.dosealerta.mensageria.infra.client;

import java.util.List;

/** Corpo do 422 devolvido por `POST /receitas/confirmar-por-telefone` quando falta dose/frequência/duração. */
record DadosReceitaIncompletosResponse(String mensagem, List<String> camposPendentes) {
}
