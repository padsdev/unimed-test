import type { Atendimento, Paciente, Procedimento } from "@/types/domain";

import { withMockLatency } from "./api-client";
import { atendimentosMock, pacientesMock, procedimentosMock } from "./mock-data";

export const clinicalServices = {
  listPacientes: (): Promise<Paciente[]> => withMockLatency(pacientesMock),
  listAtendimentos: (): Promise<Atendimento[]> => withMockLatency(atendimentosMock),
  listProcedimentos: (): Promise<Procedimento[]> => withMockLatency(procedimentosMock),
};
