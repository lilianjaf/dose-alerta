CREATE TABLE interacao (
    id UUID PRIMARY KEY,
    paciente_id UUID NOT NULL,
    medicamento VARCHAR(255) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    registrada_em TIMESTAMP NOT NULL
);

CREATE INDEX idx_interacao_paciente_registrada_em ON interacao (paciente_id, registrada_em);
