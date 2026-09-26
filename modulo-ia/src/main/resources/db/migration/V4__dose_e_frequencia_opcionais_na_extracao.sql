-- Dose e frequência, como a duração, podem não constar (ou não ser legíveis) na receita: ficam nulas até o
-- paciente informar na confirmação. Os valores confirmados continuam obrigatórios.
ALTER TABLE receita ALTER COLUMN dose DROP NOT NULL;
ALTER TABLE receita ALTER COLUMN frequencia_horas DROP NOT NULL;
ALTER TABLE feedback_extracao ALTER COLUMN dose_extraida DROP NOT NULL;
ALTER TABLE feedback_extracao ALTER COLUMN frequencia_extraida_horas DROP NOT NULL;
