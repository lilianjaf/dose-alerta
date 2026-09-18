CREATE TABLE evento_interacao_outbox (
    id UUID PRIMARY KEY,
    alarme_id UUID NOT NULL REFERENCES alarme (id),
    paciente_id UUID NOT NULL,
    medicamento VARCHAR(255) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    registrada_em TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    publicado_em TIMESTAMP
);

CREATE INDEX idx_evento_interacao_outbox_status ON evento_interacao_outbox (status);
