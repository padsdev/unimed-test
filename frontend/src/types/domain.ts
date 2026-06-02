export type StatusTone = "normal" | "success" | "warning" | "danger";

export interface Paciente {
  id: number;
  nome: string;
  cpf: string;
  telefone: string;
  email: string;
}

export interface Atendimento {
  id: number;
  pacienteId: number;
  dataAtendimento: string;
  medico: string;
  observacoes: string;
}

export interface Procedimento {
  id: number;
  atendimentoId: number;
  nome: string;
  valor: number;
}
