import { useQuery } from "@tanstack/react-query";

import { clinicalServices } from "@/services/clinical-services";

export function useHistoricoPaciente(pacienteId: number | null, sortOrder: "desc" | "asc" = "desc") {
  return useQuery({
    queryKey: ["historico", pacienteId, sortOrder],
    queryFn: () => clinicalServices.historico.getByPaciente(pacienteId!, sortOrder),
    enabled: pacienteId !== null,
  });
}
