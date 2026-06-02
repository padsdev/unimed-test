import type { ModuleStatus } from "@/types/domain";

export const moduleStatus: Record<string, ModuleStatus> = {
  pacientes: "mock",
  atendimentos: "mock",
  procedimentos: "mock",
  historico: "mock",
};

export type ModuleName = keyof typeof moduleStatus;
