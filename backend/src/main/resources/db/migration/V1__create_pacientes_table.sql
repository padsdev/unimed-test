CREATE TABLE IF NOT EXISTS pacientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    data_nascimento DATE NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    email VARCHAR(180) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ativo'
);

CREATE INDEX IF NOT EXISTS idx_pacientes_nome ON pacientes (nome);
