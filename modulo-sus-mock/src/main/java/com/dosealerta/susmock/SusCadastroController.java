package com.dosealerta.susmock;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SusCadastroController {

	private static final Map<String, String> CADASTROS_FIXOS =
			Map.of("+5511999990001", "Ana Paula Souza", "+5511999990002", "Carlos Eduardo Lima");
	private static final Map<String, String> INSCRICOES_FIXAS =
			Map.of("700000000000001", "Fernanda Torres Lima", "700000000000002", "Ricardo Alves Nogueira");

	@GetMapping("/sus/cadastros/{telefone}")
	CadastroSusOutput consultarPorTelefone(@PathVariable String telefone) {
		return new CadastroSusOutput(telefone, buscar(CADASTROS_FIXOS, telefone));
	}

	@GetMapping("/sus/inscricoes/{numero}")
	InscricaoSusOutput consultarPorNumeroInscricao(@PathVariable String numero) {
		return new InscricaoSusOutput(numero, buscar(INSCRICOES_FIXAS, numero));
	}

	private String buscar(Map<String, String> cadastros, String chave) {
		String nomeCompleto = cadastros.get(chave);
		if (nomeCompleto == null) {
			throw new CadastroSusNaoEncontradoException(chave);
		}
		return nomeCompleto;
	}
}
