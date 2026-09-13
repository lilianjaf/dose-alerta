CREATE TABLE paciente (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    CONSTRAINT uk_paciente_telefone UNIQUE (telefone)
);
