import type { Atendimento, Paciente, Procedimento } from "@/types/domain";

let nextPacienteId = 10;
let nextAtendimentoId = 20;
let nextProcedimentoId = 200;

export const pacientesMock: Paciente[] = [
  { id: 1, nome: "João da Silva", cpf: "123.456.789-00", dataNascimento: "1985-03-15", telefone: "(11) 98888-1111", email: "joao.silva@email.com", status: "ativo" },
  { id: 2, nome: "Maria Oliveira", cpf: "987.654.321-00", dataNascimento: "1992-07-22", telefone: "(11) 97777-2222", email: "maria.oliveira@email.com", status: "ativo" },
  { id: 3, nome: "Carlos Pereira", cpf: "456.789.123-00", dataNascimento: "1978-11-02", telefone: "(11) 96666-3333", email: "carlos.pereira@email.com", status: "ativo" },
  { id: 4, nome: "Ana Costa", cpf: "321.654.987-00", dataNascimento: "1995-01-30", telefone: "(11) 95555-4444", email: "ana.costa@email.com", status: "inativo" },
  { id: 5, nome: "Pedro Santos", cpf: "789.123.456-00", dataNascimento: "1980-09-12", telefone: "(11) 94444-5555", email: "pedro.santos@email.com", status: "ativo" },
];

export const atendimentosMock: Atendimento[] = [
  { id: 10, pacienteId: 1, dataAtendimento: "2026-05-01T12:00:00Z", medico: "Dr. Carlos Andrade", observacoes: "Dor torácica em monitoramento" },
  { id: 11, pacienteId: 1, dataAtendimento: "2026-04-18T15:30:00Z", medico: "Dra. Juliana Moraes", observacoes: "Retorno — paciente assintomático" },
  { id: 12, pacienteId: 2, dataAtendimento: "2026-05-18T15:30:00Z", medico: "Dra. Juliana Moraes", observacoes: "Revisão clínica de rotina" },
  { id: 13, pacienteId: 2, dataAtendimento: "2026-03-10T09:00:00Z", medico: "Dr. Paulo Nunes", observacoes: "Exame periódico ocupacional" },
  { id: 14, pacienteId: 3, dataAtendimento: "2026-05-22T10:00:00Z", medico: "Dr. Carlos Andrade", observacoes: "Hipertensão — ajuste de medicação" },
  { id: 15, pacienteId: 5, dataAtendimento: "2026-05-20T14:00:00Z", medico: "Dra. Fernanda Lima", observacoes: "Check-up anual" },
];

export const procedimentosMock: Procedimento[] = [
  { id: 100, atendimentoId: 10, nome: "Eletrocardiograma", valor: 120.0 },
  { id: 101, atendimentoId: 10, nome: "Consulta cardiológica", valor: 250.0 },
  { id: 102, atendimentoId: 11, nome: "Aferição de pressão", valor: 30.0 },
  { id: 103, atendimentoId: 12, nome: "Painel laboratorial", valor: 190.0 },
  { id: 104, atendimentoId: 13, nome: "Exame de urina", valor: 45.0 },
  { id: 105, atendimentoId: 13, nome: "Hemograma completo", valor: 80.0 },
  { id: 106, atendimentoId: 14, nome: "Consulta de rotina", valor: 180.0 },
  { id: 107, atendimentoId: 14, nome: "Eletrocardiograma", valor: 120.0 },
  { id: 108, atendimentoId: 15, nome: "Check-up executivo", valor: 350.0 },
  { id: 109, atendimentoId: 15, nome: "Exame de sangue", valor: 95.0 },
];

export function seedIds(): void {
  nextPacienteId = Math.max(...pacientesMock.map(p => p.id), 0) + 1;
  nextAtendimentoId = Math.max(...atendimentosMock.map(a => a.id), 0) + 1;
  nextProcedimentoId = Math.max(...procedimentosMock.map(p => p.id), 0) + 1;
}

export function novoIdPaciente(): number {
  return nextPacienteId++;
}

export function novoIdAtendimento(): number {
  return nextAtendimentoId++;
}

export function novoIdProcedimento(): number {
  return nextProcedimentoId++;
}
