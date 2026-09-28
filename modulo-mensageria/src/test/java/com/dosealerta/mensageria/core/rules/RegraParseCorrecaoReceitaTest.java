package com.dosealerta.mensageria.core.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.dosealerta.mensageria.core.dto.CorrecaoReceita;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RegraParseCorrecaoReceitaTest {

	@Test
	void deveParsearDoseFrequenciaEDuracaoSeparadosPorPontoEVirgula() {
		Optional<CorrecaoReceita> resultado = RegraParseCorrecaoReceita.parsear("1 comprimido; 8; 7");

		assertTrue(resultado.isPresent());
		assertEquals("1 comprimido", resultado.get().dose());
		assertEquals(8, resultado.get().frequenciaHoras());
		assertEquals(7, resultado.get().duracaoDias());
	}

	@Test
	void deveIgnorarEspacosEmVoltaDeCadaCampo() {
		Optional<CorrecaoReceita> resultado = RegraParseCorrecaoReceita.parsear("  2 doses  ;  6 ;  30  ");

		assertTrue(resultado.isPresent());
		assertEquals("2 doses", resultado.get().dose());
		assertEquals(6, resultado.get().frequenciaHoras());
		assertEquals(30, resultado.get().duracaoDias());
	}

	@Test
	void deveDevolverVazioQuandoNaoTemExatamenteTresCampos() {
		assertTrue(RegraParseCorrecaoReceita.parsear("1 comprimido; 8").isEmpty());
		assertTrue(RegraParseCorrecaoReceita.parsear("1 comprimido; 8; 7; 30").isEmpty());
		assertTrue(RegraParseCorrecaoReceita.parsear("sim").isEmpty());
		assertTrue(RegraParseCorrecaoReceita.parsear(null).isEmpty());
	}

	@Test
	void deveDevolverVazioQuandoFrequenciaOuDuracaoNaoSaoNumeros() {
		assertTrue(RegraParseCorrecaoReceita.parsear("1 comprimido; oito; 7").isEmpty());
		assertTrue(RegraParseCorrecaoReceita.parsear("1 comprimido; 8; sete").isEmpty());
	}

	@Test
	void deveDevolverVazioQuandoADoseEstaVazia() {
		assertTrue(RegraParseCorrecaoReceita.parsear(" ; 8; 7").isEmpty());
	}
}
