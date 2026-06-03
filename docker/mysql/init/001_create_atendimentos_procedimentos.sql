CREATE TABLE IF NOT EXISTS atendimentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    data_atendimento DATETIME NOT NULL,
    medico VARCHAR(200) NOT NULL,
    observacoes TEXT NOT NULL,
    INDEX idx_atendimentos_paciente_id (paciente_id),
    INDEX idx_atendimentos_data (data_atendimento)
);

CREATE TABLE IF NOT EXISTS procedimentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    atendimento_id BIGINT NOT NULL,
    nome VARCHAR(200) NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_procedimentos_atendimentos
        FOREIGN KEY (atendimento_id)
        REFERENCES atendimentos(id)
        ON DELETE CASCADE,
    INDEX idx_procedimentos_atendimento_id (atendimento_id)
);
