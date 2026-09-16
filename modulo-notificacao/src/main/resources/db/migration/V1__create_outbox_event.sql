CREATE TABLE outbox_event (
    id UUID PRIMARY KEY,
    alarme_id UUID NOT NULL,
    paciente_id UUID NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    medicamento VARCHAR(255) NOT NULL,
    dose VARCHAR(100) NOT NULL,
    etapa VARCHAR(30) NOT NULL,
    canal VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    publicado_em TIMESTAMP
);

CREATE INDEX idx_outbox_event_status ON outbox_event (status);
