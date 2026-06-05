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
import {
  atendimentosMock,
  novoIdAtendimento,
  novoIdPaciente,
  novoIdProcedimento,
  pacientesMock,
  procedimentosMock,
} from "./mock-data";
import { moduleStatus } from "./module-status";

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "";

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const body = await res.json().catch(() => null);
    const msg = body?.detail ?? body?.title ?? `HTTP ${res.status}`;
    throw new Error(msg);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

function pacientesList(filters: PacientesFilters): Promise<PagedResponse<Paciente>> {
  const params = new URLSearchParams();
  params.set("page", String(filters.page ?? 0));
  params.set("size", String(filters.size ?? 20));
  if (filters.q) params.set("q", filters.q);
  return fetch(`${API_BASE}/api/pacientes?${params}`).then(handleResponse<PagedResponse<Paciente>>);
}

function pacientesGetById(id: number): Promise<Paciente> {
  return fetch(`${API_BASE}/api/pacientes/${id}`).then(handleResponse<Paciente>);
}

function pacientesCreate(data: CriarPacienteRequest): Promise<Paciente> {
  return fetch(`${API_BASE}/api/pacientes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(handleResponse<Paciente>);
}

function pacientesUpdate(id: number, data: AtualizarPacienteRequest): Promise<Paciente> {
  return fetch(`${API_BASE}/api/pacientes/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(handleResponse<Paciente>);
}

function pacientesDelete(id: number): Promise<void> {
  return fetch(`${API_BASE}/api/pacientes/${id}`, { method: "DELETE" }).then(handleResponse<void>);
}

function getPacientesService() {
  const status = moduleStatus.pacientes;
  if (status === "api" || status === "hybrid") {
    return {
      list: pacientesList,
      getById: pacientesGetById,
      create: pacientesCreate,
      update: pacientesUpdate,
      delete: pacientesDelete,
    };
  }
  return pacientesMockService;
}

const pacientesMockService = {
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
    if (!found) return Promise.reject(new Error("Paciente n\u00e3o encontrado"));
    return withMockLatency(found);
  },
  create: (data: CriarPacienteRequest): Promise<Paciente> => {
    const novo: Paciente = { id: novoIdPaciente(), ...data };
    pacientesMock.push(novo);
    return withMockLatency(novo);
  },
  update: (id: number, data: AtualizarPacienteRequest): Promise<Paciente> => {
    const index = pacientesMock.findIndex((p) => p.id === id);
    if (index === -1) return Promise.reject(new Error("Paciente n\u00e3o encontrado"));
    pacientesMock[index] = { ...pacientesMock[index], ...data };
    return withMockLatency(pacientesMock[index]);
  },
  delete: (id: number): Promise<void> => {
    const index = pacientesMock.findIndex((p) => p.id === id);
    if (index === -1) return Promise.reject(new Error("Paciente n\u00e3o encontrado"));
    const hasVinculos = atendimentosMock.some((a) => a.pacienteId === id);
    if (hasVinculos) return Promise.reject(new Error("Paciente possui v\u00ednculos ativos"));
    pacientesMock.splice(index, 1);
    return withMockLatency(undefined);
  },
};

function atendimentosList(filters: AtendimentosFilters): Promise<PagedResponse<Atendimento>> {
  const params = new URLSearchParams();
  params.set("page", String(filters.page ?? 0));
  params.set("size", String(filters.size ?? 20));
  if (filters.q) params.set("q", filters.q);
  if (filters.pacienteId) params.set("pacienteId", String(filters.pacienteId));
  return fetch(`${API_BASE}/api/atendimentos?${params}`).then(handleResponse<PagedResponse<Atendimento>>);
}

function atendimentosGetById(id: number): Promise<Atendimento> {
  return fetch(`${API_BASE}/api/atendimentos/${id}`).then(handleResponse<Atendimento>);
}

function atendimentosCreate(data: CriarAtendimentoRequest): Promise<Atendimento> {
  return fetch(`${API_BASE}/api/atendimentos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(handleResponse<Atendimento>);
}

function atendimentosUpdate(id: number, data: AtualizarAtendimentoRequest): Promise<Atendimento> {
  return fetch(`${API_BASE}/api/atendimentos/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(handleResponse<Atendimento>);
}

function atendimentosDelete(id: number): Promise<void> {
  return fetch(`${API_BASE}/api/atendimentos/${id}`, { method: "DELETE" }).then(handleResponse<void>);
}

function getAtendimentosService() {
  const status = moduleStatus.atendimentos;
  if (status === "api" || status === "hybrid") {
    return {
      list: atendimentosList,
      getById: atendimentosGetById,
      create: atendimentosCreate,
      update: atendimentosUpdate,
      delete: atendimentosDelete,
    };
  }
  return atendimentosMockService;
}

const atendimentosMockService = {
  list: (filters: AtendimentosFilters): Promise<PagedResponse<Atendimento>> => {
    const { q, pacienteId, ...pagination } = filters;
    let filtered = atendimentosMock;
    if (pacienteId) filtered = filtered.filter((a) => a.pacienteId === pacienteId);
    if (q) filtered = filtered.filter((a) => matchQuery(a as unknown as Record<string, unknown>, q));
    return withMockLatency(paginate(filtered, pagination));
  },
  getById: (id: number): Promise<Atendimento> => {
    const found = atendimentosMock.find((a) => a.id === id);
    if (!found) return Promise.reject(new Error("Atendimento n\u00e3o encontrado"));
    return withMockLatency(found);
  },
  create: (data: CriarAtendimentoRequest): Promise<Atendimento> => {
    const novo: Atendimento = { id: novoIdAtendimento(), ...data };
    atendimentosMock.push(novo);
    return withMockLatency(novo);
  },
  update: (id: number, data: AtualizarAtendimentoRequest): Promise<Atendimento> => {
    const index = atendimentosMock.findIndex((a) => a.id === id);
    if (index === -1) return Promise.reject(new Error("Atendimento n\u00e3o encontrado"));
    atendimentosMock[index] = { ...atendimentosMock[index], ...data };
    return withMockLatency(atendimentosMock[index]);
  },
  delete: (id: number): Promise<void> => {
    const index = atendimentosMock.findIndex((a) => a.id === id);
    if (index === -1) return Promise.reject(new Error("Atendimento n\u00e3o encontrado"));
    for (let i = procedimentosMock.length - 1; i >= 0; i--) {
      if (procedimentosMock[i].atendimentoId === id) procedimentosMock.splice(i, 1);
    }
    atendimentosMock.splice(index, 1);
    return withMockLatency(undefined);
  },
};

function procedimentosList(filters: ProcedimentosFilters): Promise<PagedResponse<Procedimento>> {
  const params = new URLSearchParams();
  params.set("page", String(filters.page ?? 0));
  params.set("size", String(filters.size ?? 20));
  if (filters.q) params.set("q", filters.q);
  if (filters.atendimentoId) params.set("atendimentoId", String(filters.atendimentoId));
  return fetch(`${API_BASE}/api/procedimentos?${params}`).then(handleResponse<PagedResponse<Procedimento>>);
}

function procedimentosGetById(id: number): Promise<Procedimento> {
  return fetch(`${API_BASE}/api/procedimentos/${id}`).then(handleResponse<Procedimento>);
}

function procedimentosCreate(data: CriarProcedimentoRequest): Promise<Procedimento> {
  return fetch(`${API_BASE}/api/procedimentos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(handleResponse<Procedimento>);
}

function procedimentosUpdate(id: number, data: AtualizarProcedimentoRequest): Promise<Procedimento> {
  return fetch(`${API_BASE}/api/procedimentos/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  }).then(handleResponse<Procedimento>);
}

function procedimentosDelete(id: number): Promise<void> {
  return fetch(`${API_BASE}/api/procedimentos/${id}`, { method: "DELETE" }).then(handleResponse<void>);
}

function getProcedimentosService() {
  const status = moduleStatus.procedimentos;
  if (status === "api" || status === "hybrid") {
    return {
      list: procedimentosList,
      getById: procedimentosGetById,
      create: procedimentosCreate,
      update: procedimentosUpdate,
      delete: procedimentosDelete,
    };
  }
  return procedimentosMockService;
}

const procedimentosMockService = {
  list: (filters: ProcedimentosFilters): Promise<PagedResponse<Procedimento>> => {
    const { q, atendimentoId, ...pagination } = filters;
    let filtered = procedimentosMock;
    if (atendimentoId) filtered = filtered.filter((p) => p.atendimentoId === atendimentoId);
    if (q) filtered = filtered.filter((p) => matchQuery(p as unknown as Record<string, unknown>, q));
    return withMockLatency(paginate(filtered, pagination));
  },
  getById: (id: number): Promise<Procedimento> => {
    const found = procedimentosMock.find((p) => p.id === id);
    if (!found) return Promise.reject(new Error("Procedimento n\u00e3o encontrado"));
    return withMockLatency(found);
  },
  create: (data: CriarProcedimentoRequest): Promise<Procedimento> => {
    const novo: Procedimento = { id: novoIdProcedimento(), ...data };
    procedimentosMock.push(novo);
    return withMockLatency(novo);
  },
  update: (id: number, data: AtualizarProcedimentoRequest): Promise<Procedimento> => {
    const index = procedimentosMock.findIndex((p) => p.id === id);
    if (index === -1) return Promise.reject(new Error("Procedimento n\u00e3o encontrado"));
    procedimentosMock[index] = { ...procedimentosMock[index], ...data };
    return withMockLatency(procedimentosMock[index]);
  },
  delete: (id: number): Promise<void> => {
    const index = procedimentosMock.findIndex((p) => p.id === id);
    if (index === -1) return Promise.reject(new Error("Procedimento n\u00e3o encontrado"));
    procedimentosMock.splice(index, 1);
    return withMockLatency(undefined);
  },
};

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

function historicoGetByPaciente(pacienteId: number, sortOrder: "desc" | "asc" = "desc"): Promise<HistoricoPaciente> {
  return fetch(`${API_BASE}/api/pacientes/${pacienteId}/historico?sortOrder=${sortOrder}`).then(handleResponse<HistoricoPaciente>);
}

function getHistoricoService() {
  const status = moduleStatus.historico;
  if (status === "api" || status === "hybrid") {
    return { getByPaciente: historicoGetByPaciente };
  }
  return historicoMockService;
}

const historicoMockService = {
  getByPaciente: (pacienteId: number, sortOrder: "desc" | "asc" = "desc"): Promise<HistoricoPaciente> => {
    const paciente = pacientesMock.find((p) => p.id === pacienteId);
    if (!paciente) return Promise.reject(new Error("Paciente n\u00e3o encontrado"));
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
};

export const clinicalServices = {
  pacientes: getPacientesService(),
  atendimentos: getAtendimentosService(),
  procedimentos: getProcedimentosService(),
  historico: getHistoricoService(),
};
