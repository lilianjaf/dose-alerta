-- A receita pode não informar a duração do tratamento: nesse caso ela fica nula até o paciente informar
-- na confirmação. A duração confirmada continua obrigatória.
ALTER TABLE receita ALTER COLUMN duracao_dias DROP NOT NULL;
ALTER TABLE feedback_extracao ALTER COLUMN duracao_extraida_dias DROP NOT NULL;
