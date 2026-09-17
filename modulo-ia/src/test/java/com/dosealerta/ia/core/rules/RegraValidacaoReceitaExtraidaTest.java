package com.dosealerta.ia.core.rules;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import org.junit.jupiter.api.Test;

class RegraValidacaoReceitaExtraidaTest {

	@Test
	void deveAceitarExtracaoValida() {
		RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 24, 30));
	}

	@Test
	void deveAceitarDoseComVariasUnidadesConhecidas() {
		RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Paracetamol", "1 comprimido", 8, 5));
		RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Insulina", "10 UI", 12, 90));
		RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Soro", "5 gotas", 6, 10));
	}

	@Test
	void deveRejeitarMedicamentoEmBranco() {
		assertThrows(
				ReceitaInvalidaException.class, () -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("", "50mg", 24, 30)));
	}

	@Test
	void deveRejeitarDoseForaDoFormatoEsperado() {
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "muito", 24, 30)));
	}

	@Test
	void deveRejeitarFrequenciaForaDaFaixa() {
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 0, 30)));
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 25, 30)));
	}

	@Test
	void deveRejeitarDuracaoForaDaFaixa() {
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 24, 0)));
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 24, 366)));
	}
}
