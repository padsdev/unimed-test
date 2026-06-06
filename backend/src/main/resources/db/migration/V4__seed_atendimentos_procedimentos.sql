DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'atendimentos'
    ) THEN
        INSERT INTO atendimentos (paciente_id, data_atendimento, medico, observacoes) VALUES
        (1, '2026-01-15 09:00:00', 'Dra. Mariana Costa', 'Paciente relata dores de cabeca frequentes ha 2 semanas. Prescrito exames complementares.'),
        (2, '2026-02-20 14:30:00', 'Dr. Roberto Almeida', 'Check-up anual. Exames laboratoriais solicitados. Pressao arterial normal.'),
        (3, '2026-03-10 10:15:00', 'Dra. Juliana Mendes', 'Paciente com suspeita de alergia sazonal. Prescrito anti-histaminico.'),
        (4, '2026-04-05 08:45:00', 'Dr. Fernando Lima', 'Avaliacao ortopedica devido a dor no joelho direito. Solicita ressonancia magnetica.'),
        (5, '2026-05-22 16:00:00', 'Dra. Mariana Costa', 'Retorno para resultados de exames. Glicemia elevada. Orientacao nutricional fornecida.');
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'procedimentos'
    ) THEN
        INSERT INTO procedimentos (atendimento_id, nome, valor) VALUES
        (1, 'Consulta clinica geral', 150.00),
        (1, 'Eletrocardiograma', 80.00),
        (2, 'Exame de sangue hemograma', 45.00),
        (3, 'Raio-X toracico', 120.00),
        (5, 'Retorno consulta', 100.00);
    END IF;
END $$;
