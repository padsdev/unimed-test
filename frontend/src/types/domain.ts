export type StatusTone = "normal" | "success" | "warning" | "danger";

export type ModuleStatus = "mock" | "hybrid" | "api";

export interface Paciente {
  id: number;
  nome: string;
  cpf: string;
  dataNascimento: string;
  telefone: string;
  email: string;
}

export interface CriarPacienteRequest {
  nome: string;
  cpf: string;
  dataNascimento: string;
  telefone: string;
  email: string;
}

export interface AtualizarPacienteRequest {
  nome: string;
  dataNascimento: string;
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

export interface CriarAtendimentoRequest {
  pacienteId: number;
  dataAtendimento: string;
  medico: string;
  observacoes: string;
}

export interface AtualizarAtendimentoRequest {
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

export interface CriarProcedimentoRequest {
  atendimentoId: number;
  nome: string;
  valor: number;
}

export interface AtualizarProcedimentoRequest {
  nome: string;
  valor: number;
}

export interface AtendimentoComProcedimentos extends Atendimento {
  procedimentos: Procedimento[];
}

export interface HistoricoPaciente {
  paciente: Paciente;
  atendimentos: AtendimentoComProcedimentos[];
}

export interface PagedResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}

export interface PacientesFilters extends PageRequest {
  q?: string;
}

export interface AtendimentosFilters extends PageRequest {
  q?: string;
  pacienteId?: number;
}

export interface ProcedimentosFilters extends PageRequest {
  q?: string;
  atendimentoId?: number;
}

export interface FieldError {
  field: string;
  message: string;
  code: string;
}

export interface ProblemDetail {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance?: string;
  details?: FieldError[];
}
