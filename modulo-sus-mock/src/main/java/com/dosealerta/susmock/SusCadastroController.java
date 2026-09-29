package com.dosealerta.susmock;

import java.util.Map;
import org.springframework.http.ResponseEntity;
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
	ResponseEntity<CadastroSusOutput> consultarPorTelefone(@PathVariable String telefone) {
		String nomeCompleto = CADASTROS_FIXOS.get(telefone);
		if (nomeCompleto == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new CadastroSusOutput(telefone, nomeCompleto));
	}

	@GetMapping("/sus/inscricoes/{numero}")
	ResponseEntity<InscricaoSusOutput> consultarPorNumeroInscricao(@PathVariable String numero) {
		String nomeCompleto = INSCRICOES_FIXAS.get(numero);
		if (nomeCompleto == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new InscricaoSusOutput(numero, nomeCompleto));
	}
}
