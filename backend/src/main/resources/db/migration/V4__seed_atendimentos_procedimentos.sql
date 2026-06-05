INSERT INTO atendimentos (paciente_id, data_atendimento, medico, observacoes) VALUES
(1, '2026-01-15 09:00:00', 'Dra. Mariana Costa', 'Paciente relata dores de cabeça frequentes há 2 semanas. Prescrito exames complementares.'),
(2, '2026-02-20 14:30:00', 'Dr. Roberto Almeida', 'Check-up anual. Exames laboratoriais solicitados. Pressão arterial normal.'),
(3, '2026-03-10 10:15:00', 'Dra. Juliana Mendes', 'Paciente com suspeita de alergia sazonal. Prescrito anti-histamínico.'),
(4, '2026-04-05 08:45:00', 'Dr. Fernando Lima', 'Avaliação ortopédica devido a dor no joelho direito. Solicita ressonância magnética.'),
(5, '2026-05-22 16:00:00', 'Dra. Mariana Costa', 'Retorno para resultados de exames. Glicemia elevada. Orientação nutricional fornecida.');

INSERT INTO procedimentos (atendimento_id, nome, valor) VALUES
(1, 'Consulta clínica geral', 150.00),
(1, 'Eletrocardiograma', 80.00),
(2, 'Exame de sangue hemograma', 45.00),
(3, 'Raio-X torácico', 120.00),
(5, 'Retorno consulta', 100.00);