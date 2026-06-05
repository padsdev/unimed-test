import type { ModuleStatus } from "@/types/domain";

export const moduleStatus: Record<string, ModuleStatus> = {
  pacientes: "api",
  atendimentos: "api",
  procedimentos: "api",
  historico: "api",
};

export type ModuleName = keyof typeof moduleStatus;
