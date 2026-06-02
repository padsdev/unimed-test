import type { Atendimento, Paciente, Procedimento } from "@/types/domain";

export const pacientesMock: Paciente[] = [
  {
    id: 1,
    nome: "Joao da Silva",
    cpf: "123.456.789-00",
    telefone: "(11) 98888-1111",
    email: "joao.silva@email.com",
  },
  {
    id: 2,
    nome: "Maria Oliveira",
    cpf: "987.654.321-00",
    telefone: "(11) 97777-2222",
    email: "maria.oliveira@email.com",
  },
];

export const atendimentosMock: Atendimento[] = [
  {
    id: 10,
    pacienteId: 1,
    dataAtendimento: "2026-05-01T12:00:00Z",
    medico: "Dr. Carlos Andrade",
    observacoes: "Dor toracica em monitoramento",
  },
  {
    id: 11,
    pacienteId: 2,
    dataAtendimento: "2026-05-18T15:30:00Z",
    medico: "Dra. Juliana Moraes",
    observacoes: "Revisao clinica de rotina",
  },
];

export const procedimentosMock: Procedimento[] = [
  {
    id: 100,
    atendimentoId: 10,
    nome: "Eletrocardiograma",
    valor: 120,
  },
  {
    id: 101,
    atendimentoId: 10,
    nome: "Consulta cardiologica",
    valor: 250,
  },
  {
    id: 102,
    atendimentoId: 11,
    nome: "Painel laboratorial",
    valor: 190,
  },
];
