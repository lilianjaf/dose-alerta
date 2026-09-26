package com.dosealerta.ia.core.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.dosealerta.ia.core.dto.ReceitaExtraida;
import com.dosealerta.ia.core.exception.ReceitaFormalNaoIdentificadaException;
import com.dosealerta.ia.core.exception.ReceitaInvalidaException;
import org.junit.jupiter.api.Test;

class RegraValidacaoReceitaExtraidaTest {

	@Test
	void deveAceitarExtracaoValida() {
		RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 24, 30, true, "Dra. Exemplo", "70760"));
	}

	@Test
	void deveAceitarDoseComVariasUnidadesConhecidas() {
		RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Paracetamol", "1 comprimido", 8, 5, true, "Dra. Exemplo", "70760"));
		RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Insulina", "10 UI", 12, 90, true, "Dra. Exemplo", "70760"));
		RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Soro", "5 gotas", 6, 10, true, "Dra. Exemplo", "70760"));
	}

	@Test
	void deveRejeitarMedicamentoEmBranco() {
		assertThrows(
				ReceitaInvalidaException.class, () -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("", "50mg", 24, 30, true, "Dra. Exemplo", "70760")));
	}

	@Test
	void deveRejeitarDoseForaDoFormatoEsperado() {
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "muito", 24, 30, true, "Dra. Exemplo", "70760")));
	}

	@Test
	void deveRejeitarFrequenciaForaDaFaixa() {
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 0, 30, true, "Dra. Exemplo", "70760")));
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 25, 30, true, "Dra. Exemplo", "70760")));
	}

	@Test
	void deveRejeitarDuracaoForaDaFaixa() {
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 24, 0, true, "Dra. Exemplo", "70760")));
		assertThrows(
				ReceitaInvalidaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(new ReceitaExtraida("Losartana", "50mg", 24, 366, true, "Dra. Exemplo", "70760")));
	}

	@Test
	void deveAceitarDoseDeMedicamentoInalatorio() {
		RegraValidacaoReceitaExtraida.validar(receita("2 doses", "Dra. Exemplo", "70.760"));
		RegraValidacaoReceitaExtraida.validar(receita("2 jatos", "Dra. Exemplo", "70.760"));
		RegraValidacaoReceitaExtraida.validar(receita("1 puff", "Dra. Exemplo", "70.760"));
		RegraValidacaoReceitaExtraida.validar(receita("2 inalações", "Dra. Exemplo", "70.760"));
	}

	@Test
	void deveAceitarCrmEmFormatosComuns() {
		RegraValidacaoReceitaExtraida.validar(receita("50mg", "Dr. João", "70760"));
		RegraValidacaoReceitaExtraida.validar(receita("50mg", "Dr. João", "CRM: 70.760"));
		RegraValidacaoReceitaExtraida.validar(receita("50mg", "Dr. João", "CRM-SP 123456"));
		RegraValidacaoReceitaExtraida.validar(receita("50mg", "Dr. João", "CRM/SP 1234"));
	}

	@Test
	void deveRejeitarImagemQueNaoEReceita() {
		ReceitaFormalNaoIdentificadaException e = assertThrows(
				ReceitaFormalNaoIdentificadaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(
						new ReceitaExtraida("Losartana", "50mg", 24, 30, false, "Dr. João", "70760")));

		assertEquals(ReceitaFormalNaoIdentificadaException.ORIENTACAO, e.getMessage());
	}

	@Test
	void deveRejeitarReceitaSemNomeDoMedico() {
		assertThrows(
				ReceitaFormalNaoIdentificadaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(receita("50mg", null, "70760")));
		assertThrows(
				ReceitaFormalNaoIdentificadaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(receita("50mg", "  ", "70760")));
	}

	@Test
	void deveRejeitarReceitaSemCrmOuComCrmImplausivel() {
		assertThrows(
				ReceitaFormalNaoIdentificadaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(receita("50mg", "Dr. João", null)));
		assertThrows(
				ReceitaFormalNaoIdentificadaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(receita("50mg", "Dr. João", "CRM")));
		assertThrows(
				ReceitaFormalNaoIdentificadaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(receita("50mg", "Dr. João", "123")));
		assertThrows(
				ReceitaFormalNaoIdentificadaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(receita("50mg", "Dr. João", "12345678")));
	}

	@Test
	void deveExigirReceitaFormalAntesDeValidarOsDemaisCampos() {
		assertThrows(
				ReceitaFormalNaoIdentificadaException.class,
				() -> RegraValidacaoReceitaExtraida.validar(
						new ReceitaExtraida(null, null, 0, 0, false, null, null)));
	}

	private ReceitaExtraida receita(String dose, String nomeMedico, String crm) {
		return new ReceitaExtraida("Aerolin", dose, 6, 30, true, nomeMedico, crm);
	}
}
