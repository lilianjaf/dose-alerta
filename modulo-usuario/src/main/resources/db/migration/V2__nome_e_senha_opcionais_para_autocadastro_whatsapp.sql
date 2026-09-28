-- Paciente identificado pelo WhatsApp pode não vir do SUS (autocadastro) e nunca tem senha de app: o cadastro
-- nasce só com telefone, "nome" fica nulo até a próxima mensagem responder, e "senha_hash" nunca é preenchido
-- para esses pacientes.
ALTER TABLE paciente ALTER COLUMN nome DROP NOT NULL;
ALTER TABLE paciente ALTER COLUMN senha_hash DROP NOT NULL;
