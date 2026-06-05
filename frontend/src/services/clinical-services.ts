import type {
  Atendimento,
  AtendimentosFilters,
  AtualizarAtendimentoRequest,
  AtualizarPacienteRequest,
  AtualizarProcedimentoRequest,
  CriarAtendimentoRequest,
  CriarPacienteRequest,
  CriarProcedimentoRequest,
  HistoricoPaciente,
  Paciente,
  PacientesFilters,
  PagedResponse,
  Procedimento,
  ProcedimentosFilters,
} from "@/types/domain";

import { withMockLatency } from "./api-client";
import { atendimentosMock, novoIdAtendimento, novoIdPaciente, novoIdProcedimento, pacientesMock, procedimentosMock } from "./mock-data";

function paginate<T>(items: T[], filters: { page?: number; size?: number }): PagedResponse<T> {
  const page = filters.page ?? 0;
  const size = filters.size ?? 20;
  const start = page * size;
  const paged = items.slice(start, start + size);
  return {
    items: paged,
    page,
    size,
    totalItems: items.length,
    totalPages: Math.ceil(items.length / size),
  };
}

function matchQuery(item: Record<string, unknown>, q: string): boolean {
  const lower = q.toLowerCase();
  return Object.values(item).some((v) => String(v).toLowerCase().includes(lower));
}

export const clinicalServices = {
  pacientes: {
    list: (filters: PacientesFilters): Promise<PagedResponse<Paciente>> => {
      const { q, ...pagination } = filters;
      let filtered = pacientesMock;
      if (q) {
        filtered = filtered.filter((p) => matchQuery(p as unknown as Record<string, unknown>, q));
      }
      return withMockLatency(paginate(filtered, pagination));
    },
    getById: (id: number): Promise<Paciente> => {
      const found = pacientesMock.find((p) => p.id === id);
      if (!found) return Promise.reject(new Error("Paciente não encontrado"));
      return withMockLatency(found);
    },
    create: (data: CriarPacienteRequest): Promise<Paciente> => {
      const novo: Paciente = { id: novoIdPaciente(), ...data };
      pacientesMock.push(novo);
      return withMockLatency(novo);
    },
    update: (id: number, data: AtualizarPacienteRequest): Promise<Paciente> => {
      const index = pacientesMock.findIndex((p) => p.id === id);
      if (index === -1) return Promise.reject(new Error("Paciente não encontrado"));
      pacientesMock[index] = { ...pacientesMock[index], ...data };
      return withMockLatency(pacientesMock[index]);
    },
    delete: (id: number): Promise<void> => {
      const index = pacientesMock.findIndex((p) => p.id === id);
      if (index === -1) return Promise.reject(new Error("Paciente não encontrado"));
      const hasVinculos = atendimentosMock.some((a) => a.pacienteId === id);
      if (hasVinculos) return Promise.reject(new Error("Paciente possui vínculos ativos"));
      pacientesMock.splice(index, 1);
      return withMockLatency(undefined);
    },
  },
  atendimentos: {
    list: (filters: AtendimentosFilters): Promise<PagedResponse<Atendimento>> => {
      const { q, pacienteId, ...pagination } = filters;
      let filtered = atendimentosMock;
      if (pacienteId) {
        filtered = filtered.filter((a) => a.pacienteId === pacienteId);
      }
      if (q) {
        filtered = filtered.filter((a) => matchQuery(a as unknown as Record<string, unknown>, q));
      }
      return withMockLatency(paginate(filtered, pagination));
    },
    getById: (id: number): Promise<Atendimento> => {
      const found = atendimentosMock.find((a) => a.id === id);
      if (!found) return Promise.reject(new Error("Atendimento não encontrado"));
      return withMockLatency(found);
    },
    create: (data: CriarAtendimentoRequest): Promise<Atendimento> => {
      const novo: Atendimento = { id: novoIdAtendimento(), ...data };
      atendimentosMock.push(novo);
      return withMockLatency(novo);
    },
    update: (id: number, data: AtualizarAtendimentoRequest): Promise<Atendimento> => {
      const index = atendimentosMock.findIndex((a) => a.id === id);
      if (index === -1) return Promise.reject(new Error("Atendimento não encontrado"));
      atendimentosMock[index] = { ...atendimentosMock[index], ...data };
      return withMockLatency(atendimentosMock[index]);
    },
    delete: (id: number): Promise<void> => {
      const index = atendimentosMock.findIndex((a) => a.id === id);
      if (index === -1) return Promise.reject(new Error("Atendimento não encontrado"));
      for (let i = procedimentosMock.length - 1; i >= 0; i--) {
        if (procedimentosMock[i].atendimentoId === id) {
          procedimentosMock.splice(i, 1);
        }
      }
      atendimentosMock.splice(index, 1);
      return withMockLatency(undefined);
    },
  },
  procedimentos: {
    list: (filters: ProcedimentosFilters): Promise<PagedResponse<Procedimento>> => {
      const { q, atendimentoId, ...pagination } = filters;
      let filtered = procedimentosMock;
      if (atendimentoId) {
        filtered = filtered.filter((p) => p.atendimentoId === atendimentoId);
      }
      if (q) {
        filtered = filtered.filter((p) => matchQuery(p as unknown as Record<string, unknown>, q));
      }
      return withMockLatency(paginate(filtered, pagination));
    },
    getById: (id: number): Promise<Procedimento> => {
      const found = procedimentosMock.find((p) => p.id === id);
      if (!found) return Promise.reject(new Error("Procedimento não encontrado"));
      return withMockLatency(found);
    },
    create: (data: CriarProcedimentoRequest): Promise<Procedimento> => {
      const novo: Procedimento = { id: novoIdProcedimento(), ...data };
      procedimentosMock.push(novo);
      return withMockLatency(novo);
    },
    update: (id: number, data: AtualizarProcedimentoRequest): Promise<Procedimento> => {
      const index = procedimentosMock.findIndex((p) => p.id === id);
      if (index === -1) return Promise.reject(new Error("Procedimento não encontrado"));
      procedimentosMock[index] = { ...procedimentosMock[index], ...data };
      return withMockLatency(procedimentosMock[index]);
    },
    delete: (id: number): Promise<void> => {
      const index = procedimentosMock.findIndex((p) => p.id === id);
      if (index === -1) return Promise.reject(new Error("Procedimento não encontrado"));
      procedimentosMock.splice(index, 1);
      return withMockLatency(undefined);
    },
  },
  historico: {
    getByPaciente: (pacienteId: number, sortOrder: "desc" | "asc" = "desc"): Promise<HistoricoPaciente> => {
      const paciente = pacientesMock.find((p) => p.id === pacienteId);
      if (!paciente) return Promise.reject(new Error("Paciente não encontrado"));
      const atendimentos = atendimentosMock.filter((a) => a.pacienteId === pacienteId);
      atendimentos.sort((a, b) => {
        const cmp = a.dataAtendimento.localeCompare(b.dataAtendimento);
        return sortOrder === "desc" ? -cmp : cmp;
      });
      const historico: HistoricoPaciente = {
        paciente,
        atendimentos: atendimentos.map((a) => ({
          ...a,
          procedimentos: procedimentosMock.filter((p) => p.atendimentoId === a.id),
        })),
      };
      return withMockLatency(historico);
    },
  },
};
